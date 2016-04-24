/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.jpsg;

import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public final class FunctionProcessor extends TemplateProcessor {
    // after blocks processor, before options processor
    public static final int PRIORITY =
            (Generator.BLOCKS_PROCESSOR_PRIORITY + OptionProcessor.PRIORITY) / 2;

    private static final Pattern FUNCTION_P = RegexpUtils.compile(
            "/[\\*/]f[\\*/]/([a-z]+)(/[\\*/][\\*/]/)?+|([a-z]+)/[\\*/]ef[\\*/]/");

    /**
     * template start or end of input, because template file is split by // if //s
     * in blocks preprocessor
     */
    private static final Pattern TEMPLATE_START = Pattern.compile("/[\\*/]|$");
    private static final Pattern CAMEL_CASE = Pattern.compile("(?<!^)(?=[A-Z])");

    private static String generateName(List<String> argDims, @Nullable String outDim,
            String baseName, boolean allowOperatorCollapse, boolean withParams, Context target) {
        List<Option> args = argDims.stream().map(target::getOption).collect(Collectors.toList());
        Option out = outDim != null ? target.getOption(outDim) : null;
        if (out instanceof PrimitiveType &&
                (argDims.size() == 1 && args.get(0) == out ||
                        allowOperatorCollapse && argDims.size() == 2 &&
                                argDims.get(0).equals(argDims.get(1)) &&
                                argDims.get(0).equals(outDim))) {
            String infix = argDims.size() == 1 ? "Unary" : "Binary";
            return ((PrimitiveType) out).title + infix + "Operator";
        }
        String prefix = "", infix = "";
        List<String> params = new ArrayList<>();
        for (int i = 0; i < args.size(); i++) {
            Option arg = args.get(i);
            if (arg instanceof PrimitiveType) {
                prefix += ((PrimitiveType) arg).title;
            } else {
                if (args.size() > 1)
                    prefix += ((ObjectType) arg).idStyle.title;
                params.add("? super " + argDims.get(i).substring(0, 1).toUpperCase());
            }
        }
        if (args.size() == 2 &&
                args.get(0) instanceof ObjectType && args.get(1) instanceof ObjectType) {
            prefix = "";
            infix = "Bi";
        }
        if (outDim != null) {
            if (out instanceof PrimitiveType) {
                prefix += "To" + ((PrimitiveType) out).title;
            } else {
                params.add("? extends " + outDim.substring(0, 1).toUpperCase());
            }
        }
        return prefix + infix + baseName + (withParams ? joinParams(params) : "");
    }

    private static String joinParams(List<String> params) {
        if (params.isEmpty())
            return "";
        String res = "";
        for (String param : params) {
            res += ", " + param;
        }
        return "<" + res.substring(2) + ">";
    }

    @Override
    public int priority() {
        return PRIORITY;
    }

    @Override
    protected void process(StringBuilder sb, Context source, Context target, String template) {
        Map<String, String> titleToDim = new HashMap<>();
        for (Map.Entry<? extends String, ? extends Option> e : source) {
            Option opt = e.getValue();
            if (opt instanceof PrimitiveType) {
                titleToDim.put(((PrimitiveType) opt).title, e.getKey());
            } else if (opt instanceof ObjectType) {
                titleToDim.put(((ObjectType) opt).idStyle.title, e.getKey());
            }
        }
        int prevEnd = 0;
        Matcher m = FUNCTION_P.matcher(template);
        while (m.find()) {
            String functionClass = m.group(1);
            if (functionClass == null)
                functionClass = m.group(3);
            String[] parts = CAMEL_CASE.split(functionClass);
            List<String> argDims = new ArrayList<>();
            int i = 0;
            while (titleToDim.get(parts[i]) != null) {
                argDims.add(titleToDim.get(parts[i]));
                i++;
            }
            String outDim;
            if ("To".equals(parts[i])) {
                i++;
                if (titleToDim.get(parts[i]) != null) {
                    outDim = titleToDim.get(parts[i]);
                    i++;
                } else {
                    throw MalformedTemplateException.near(template, m.start(),
                            "Function \"out\" type (after `To` infix) is not present" +
                                    "in the source context: " + source.toString());
                }
            } else {
                outDim = null;
            }
            String baseName = "";
            for (int j = i; j < parts.length; j++) {
                baseName += parts[j];
            }
            if (argDims.isEmpty()) {
                throw MalformedTemplateException.near(template, m.start(),
                        "Function should have at 1 or 2 \"input\" type params and " +
                                "0 or 1 \"out\" type param (after `To` infix)");
            }
            boolean allowOperatorCollapse;
            if ("BinaryOperator".equals(baseName) || "UnaryOperator".equals(baseName)) {
                if (argDims.size() != 1)
                    throw MalformedTemplateException.near(template, m.start(),
                            baseName + " can have only one only 1 \"input\" type param, " +
                                    argDims.size() + " given: " + argDims);
                String dim = argDims.get(0);
                if (baseName.startsWith("Binary"))
                    argDims.add(dim);
                outDim = dim;
                baseName = "Function";
                allowOperatorCollapse = true;
            } else {
                allowOperatorCollapse = false;
            }

            Matcher templateStartM = TEMPLATE_START.matcher(template.substring(m.end()));

            boolean noTemplateAhead = m.group(2) != null ||
                    (templateStartM.find() && templateStartM.start() == 0);

            postProcess(sb, source, target, template.substring(prevEnd, m.start()));
            String generatedName = generateName(
                    argDims, outDim, baseName, allowOperatorCollapse, !noTemplateAhead, target);
            sb.append(generatedName);
            prevEnd = m.end();
        }
        postProcess(sb, source, target, template.substring(prevEnd));
    }
}
