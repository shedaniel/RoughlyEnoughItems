/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022, 2023 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.shedaniel.rei.impl.client.gui.changelog;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/*
Copyright (c) 2019 Ashur Rafiev
https://github.com/ashurrafiev/JParsedown
MIT Licence: https://github.com/ashurrafiev/JParsedown/blob/master/LICENSE

This work is derived from Parsedown version 1.8.0-beta-5:
Copyright (c) 2013-2018 Emanuil Rusev
http://parsedown.org
*/
@SuppressWarnings("RegExpRedundantEscape")
public class JParseDown {
    public static final String version = "1.0.4";
    
    public static class ReferenceData {
        public String url;
        public String title;
        
        public ReferenceData(String url, String title) {
            this.url = url;
            this.title = title;
        }
    }
    
    public static class Line {
        public String body;
        public String text;
        public int indent;
        
        public Line(String line) {
            body = line;
            text = line.replaceFirst("^\\s+", "");
            indent = line.length() - text.length();
        }
    }
    
    public abstract static class Component {
        public String markup = null;
        public boolean hidden = false;
        public HashSet<Class<?>> nonNestables = new HashSet<>();
    }
    
    public interface BlockType<B extends Block> {
        Block startBlock(JParseDown parseDown, Line line, Block block);
    }
    
    public interface InlineType<L extends Inline> {
        Inline inline(JParseDown parseDown, String text, String context);
    }
    
    public abstract static class Block extends Component {
        public boolean identified = false;
        public int interrupted = 0;
        public List<Inline> inlines;
        public Boolean autoBreak = null;
        
        public boolean isContinuable() {
            return false;
        }
        
        public boolean isCompletable() {
            return false;
        }
        
        public Block continueBlock(Line line) {
            return null;
        }
        
        public Block completeBlock() {
            return null;
        }
        
        public abstract Collection<Inline> inline(JParseDown parseDown);
    }
    
    public static class BlockParagraph extends Block {
        public String text;
        
        public BlockParagraph(String text) {
            this.text = text;
        }
        
        public static Block startBlock(JParseDown parseDown, Line line, Block block) {
            return new BlockParagraph(line.text);
        }
        
        @Override
        public boolean isContinuable() {
            return false;
        }
        
        @Override
        public Block continueBlock(Line line) {
            if (interrupted > 0)
                return null;
            text += "\n" + line.text;
            return this;
        }
        
        @Override
        public Collection<Inline> inline(JParseDown parseDown) {
            return parseDown.lineElements(text, nonNestables);
        }
        
        @Override
        public String toString() {
            return "BlockParagraph{" +
                   "text='" + text + '\'' +
                   '}';
        }
    }
    
    public static class BlockCode extends Block {
        public String text;
        
        public BlockCode(String text) {
            this.text = text;
        }
        
        public static Block startBlock(JParseDown parseDown, Line line, Block block) {
            if (block != null && block instanceof BlockParagraph && block.interrupted == 0)
                return null;
            if (line.indent >= 4) {
                return new BlockCode(line.body.substring(4));
            } else
                return null;
        }
        
        @Override
        public boolean isContinuable() {
            return true;
        }
        
        @Override
        public Block continueBlock(Line line) {
            if (line.indent >= 4) {
                while (interrupted > 0) {
                    text += "\n";
                    interrupted--;
                }
                text += "\n";
                text += line.body.substring(4);
                return this;
            } else
                return null;
        }
        
        @Override
        public boolean isCompletable() {
            return true;
        }
        
        @Override
        public Block completeBlock() {
            return this;
        }
        
        @Override
        public Collection<Inline> inline(JParseDown parseDown) {
            return parseDown.lineElements(text, nonNestables);
        }
        
        @Override
        public String toString() {
            return "BlockCode{" +
                   "text='" + text + '\'' +
                   '}';
        }
    }
    
    public static class BlockComment extends Block {
        public String text;
        public boolean closed = false;
        
        public BlockComment(String text) {
            this.text = text;
        }
        
        public static Block startBlock(JParseDown parseDown, Line line, Block block) {
            if (parseDown.markupEscaped || parseDown.safeMode)
                return null;
            if (line.text.indexOf("<!--") == 0) {
                BlockComment b = new BlockComment(line.body);
                if (line.text.contains("-->"))
                    b.closed = true;
                return b;
            } else
                return null;
        }
        
        @Override
        public boolean isContinuable() {
            return true;
        }
        
        @Override
        public Block continueBlock(Line line) {
            if (closed)
                return null;
            text += "\n" + line.body;
            if (line.text.contains("-->"))
                closed = true;
            return this;
        }
        
        @Override
        public Collection<Inline> inline(JParseDown parseDown) {
            return parseDown.lineElements(text, nonNestables);
        }
        
        @Override
        public String toString() {
            return "BlockComment{" +
                   "text='" + text + '\'' +
                   '}';
        }
    }
    
    public static class BlockFencedCode extends Block {
        public final char marker;
        public final int openerLength;
        public final String infoString;
        public String text = "";
        public boolean complete = false;
        
        public BlockFencedCode(char marker, int openerLength, String infoString) {
            this.marker = marker;
            this.openerLength = openerLength;
            this.infoString = infoString;
        }
        
        public static Block startBlock(JParseDown parseDown, Line line, Block block) {
            char marker = line.text.charAt(0);
            int openerLength = startSpan(line.text, marker);
            if (openerLength < 3)
                return null;
            String infoString = line.text.substring(openerLength).trim();
            if (infoString.contains("`"))
                return null;
            return new BlockFencedCode(marker, openerLength, infoString);
        }
        
        @Override
        public boolean isContinuable() {
            return true;
        }
        
        @Override
        public Block continueBlock(Line line) {
            if (complete)
                return null;
            while (interrupted > 0) {
                text += "\n";
                interrupted--;
            }
            int len = startSpan(line.text, marker);
            if (len >= openerLength && line.text.substring(len).trim().isEmpty()) {
                if (!text.isEmpty())
                    text = text.substring(1);
                complete = true;
                return this;
            }
            text += "\n" + line.body;
            return this;
        }
        
        @Override
        public boolean isCompletable() {
            return true;
        }
        
        @Override
        public Block completeBlock() {
            return this;
        }
        
        @Override
        public Collection<Inline> inline(JParseDown parseDown) {
            return parseDown.lineElements(text, nonNestables);
        }
        
        @Override
        public String toString() {
            return "BlockFencedCode{" +
                   "infoString='" + infoString + '\'' +
                   ", text='" + text + '\'' +
                   '}';
        }
    }
    
    public static class BlockHeader extends Block {
        public final int level;
        public final String line;
        
        public BlockHeader(int level, String line) {
            this.level = level;
            this.line = line;
            this.autoBreak = true;
        }
        
        public static Block startBlock(JParseDown parseDown, Line line, Block block) {
            int level = startSpan(line.text, '#');
            if (level > 6)
                return null;
            
            String text = line.text.substring(level);
            if (parseDown.strictMode && !text.isEmpty() && text.charAt(0) != ' ')
                return null;
            text = text.trim();
            
            return new BlockHeader(level, text);
        }
        
        @Override
        public Collection<Inline> inline(JParseDown parseDown) {
            return parseDown.lineElements(line, nonNestables);
        }
        
        @Override
        public String toString() {
            return "BlockHeader{" +
                   "level=" + level +
                   ", line='" + line + '\'' +
                   '}';
        }
    }
    
    public class BlockList extends Block {
        public int indent;
        public String pattern;
        public boolean loose = false;
        
        public boolean ordered;
        public String marker;
        public String markerType;
        public String markerTypeRegex;
        
        public LinkedList<String> lines = new LinkedList<>();
    
        public BlockList() {
            autoBreak = true;
        }
    
        public static Block startBlock(JParseDown parseDown, Line line, Block block) {
            boolean ordered;
            String pattern;
            if (Character.isDigit(line.text.charAt(0))) {
                ordered = true; // ol
                pattern = "[0-9]{1,9}+[.\\)]";
            } else {
                ordered = false; // ul
                pattern = "[*+-]";
            }
            Matcher m = Pattern.compile("^(" + pattern + "([ ]++|$))(.*+)").matcher(line.text);
            if (m.find()) {
                String marker = m.group(1);
                String body = m.group(3);
                
                int contentIndent = m.group(2).length();
                if (contentIndent >= 5) {
                    contentIndent--;
                    marker = marker.substring(0, -contentIndent);
                    while (contentIndent > 0) {
                        body = " " + body;
                        contentIndent--;
                    }
                } else if (contentIndent == 0) {
                    marker += " ";
                }
                String markerWithoutWhitespace = marker.substring(0, marker.indexOf(' '));
                
                BlockList b = parseDown.new BlockList();
                b.indent = line.indent;
                b.pattern = pattern;
                b.ordered = ordered;
                b.marker = marker;
                b.markerType = !ordered ?
                        markerWithoutWhitespace :
                        markerWithoutWhitespace.substring(markerWithoutWhitespace.length() - 1, markerWithoutWhitespace.length());
                b.markerTypeRegex = Pattern.quote(b.markerType);
                
                b.lines.add(body);
                
                return b;
            } else
                return null;
        }
        
        @Override
        public boolean isContinuable() {
            return true;
        }
        
        @Override
        public Block continueBlock(Line line) {
            if (interrupted > 0 && lines.isEmpty())
                return null;
            
            int requiredIndent = indent + marker.length();
            Matcher m;
            if (line.indent < requiredIndent && (
                    (ordered && (m = Pattern.compile("^[0-9]++" + markerTypeRegex + "(?:[ ]++(.*)|$)").matcher(line.text)).find()) ||
                    (!ordered && (m = Pattern.compile("^" + markerTypeRegex + "(?:[ ]++(.*)|$)").matcher(line.text)).find())
            )) {
                if (interrupted > 0) {
                    lines.add("");
                    loose = true;
                    interrupted = 0;
                }
                String text = m.group(1) != null ? m.group(1) : "";
                indent = line.indent;
                lines.add(text);
                return this;
            } else if (line.indent < requiredIndent && BlockList.startBlock(JParseDown.this, line, null) != null) {
                return null;
            }
            
            if (line.text.charAt(0) == '[' && BlockReference.startBlock(JParseDown.this, line, null) != null) {
                return this;
            }
            
            if (line.indent >= requiredIndent) {
                if (interrupted > 0) {
                    lines.add("");
                    loose = true;
                    interrupted = 0;
                }
                String text = line.body.substring(requiredIndent);
                lines.add(text);
                return this;
            }
            
            if (interrupted == 0) {
                String text = line.body.replaceAll("^[ ]{0," + requiredIndent + "}+", "");
                lines.add(text);
                return this;
            }
            
            return null;
        }
        
        @Override
        public boolean isCompletable() {
            return true;
        }
        
        @Override
        public Block completeBlock() {
            if (loose) {
                if (!lines.getLast().isEmpty())
                    lines.add("");
            }
            return this;
        }
        
        @Override
        public Collection<Inline> inline(JParseDown parseDown) {
            LinkedList<Inline> inlines = new LinkedList<>();
            for (String line : lines) {
                if (!inlines.isEmpty())
                    inlines.add(new InlineLineBreak());
                inlines.addAll(parseDown.lineElements(line, nonNestables));
            }
            return inlines;
        }
        
        @Override
        public String toString() {
            return "BlockList{" +
                   "lines=[" + String.join(", ", lines) +
                   "]}";
        }
    }
    
    public static class BlockQuote extends Block {
        public final LinkedList<String> lines = new LinkedList<>();
        
        public BlockQuote(String text) {
            if (text != null)
                lines.add(text);
        }
        
        public static Block startBlock(JParseDown parseDown, Line line, Block block) {
            Matcher m;
            if ((m = Pattern.compile("^>[ ]?+(.*+)").matcher(line.text)).find()) {
                return new BlockQuote(m.group(1));
            } else
                return null;
        }
        
        @Override
        public boolean isContinuable() {
            return true;
        }
        
        @Override
        public Block continueBlock(Line line) {
            if (interrupted > 0)
                return null;
            Matcher m;
            if (line.text.charAt(0) == '>' && (m = Pattern.compile("^>[ ]?+(.*+)").matcher(line.text)).find()) {
                lines.add(m.group(1));
                return this;
            }
            if (interrupted == 0) {
                lines.add(line.text);
                return this;
            }
            return null;
        }
        
        @Override
        public Collection<Inline> inline(JParseDown parseDown) {
            LinkedList<Inline> inlines = new LinkedList<>();
            for (String line : lines) {
                if (!inlines.isEmpty())
                    inlines.add(new InlineLineBreak());
                inlines.addAll(parseDown.lineElements(line, nonNestables));
            }
            return inlines;
        }
        
        @Override
        public String toString() {
            return "BlockQuote{" +
                   "lines=[" + String.join(", ", lines) +
                   "]}";
        }
    }
    
    public static class BlockRule {
        public static Block startBlock(JParseDown parseDown, Line line, Block block) {
            char marker = line.text.charAt(0);
            int count = startSpan(line.text, marker);
            if (count >= 3 && line.text.trim().length() == count) {
                return new BlockHorizontalRule();
            } else
                return null;
        }
    }
    
    public static class BlockHorizontalRule extends Block {
        public BlockHorizontalRule() {
            autoBreak = true;
        }
    
        @Override
        public Collection<Inline> inline(JParseDown parseDown) {
            return Collections.singletonList(new InlineHorizontalRule());
        }
        
        @Override
        public String toString() {
            return "BlockHorizontalRule{}";
        }
    }
    
    public static class InlineLineBreak extends Inline {
        @Override
        public String toString() {
            return "InlineLineBreak{}";
        }
    }
    
    public static class InlineHorizontalRule extends Inline {
        @Override
        public String toString() {
            return "InlineHorizontalRule{}";
        }
    }
    
    public static String regexHtmlAttribute = "[a-zA-Z_:][\\w:.-]*+(?:\\s*+=\\s*+(?:[^\"\\'=<>`\\s]+|\"[^\"]*+\"|\\'[^\\']*+\\'))?+";
    
    public static class BlockReference extends Block {
        public final String id;
        public final ReferenceData data;
        
        public BlockReference(String id, ReferenceData data) {
            this.id = id;
            this.data = data;
        }
        
        public static Block startBlock(JParseDown parseDown, Line line, Block block) {
            Matcher m;
            if (line.text.indexOf(']') >= 0 && (m = Pattern.compile("^\\[(.+?)\\]:[ ]*+<?(\\S+?)>?(?:[ ]+[\"\\'(](.+)[\"\\')])?[ ]*+$").matcher(line.text)).find()) {
                String id = m.group(1).toLowerCase();
                ReferenceData data = new ReferenceData(parseDown.convertUrl(m.group(2)), m.group(3));
                parseDown.referenceDefinitions.put(id, data);
                return new BlockReference(id, data);
            } else
                return null;
        }
        
        @Override
        public Collection<Inline> inline(JParseDown parseDown) {
            return Collections.emptyList();
        }
        
        @Override
        public String toString() {
            return "BlockReference{" +
                   "id='" + id + '\'' +
                   '}';
        }
    }
    
    public abstract static class Inline extends Component {
        public int extent;
        public int position = -1;
        
        public Inline() {
        }
        
        public Inline setExtent(String s) {
            this.extent = s.length();
            return this;
        }
        
        public Inline setExtent(int len) {
            this.extent = len;
            return this;
        }
    }
    
    public static class InlineText extends Inline {
        public final String text;
        
        public InlineText(String text) {
            this.text = text;
        }
        
        public static Collection<Inline> inline(JParseDown parseDown, String text, String context) {
            return replaceAllElements(
                    parseDown.breaksEnabled ? "[ ]*+\\n" : "(?:[ ]*+\\\\|[ ]{2,}+)\\n",
                    new Inline[]{
                            new InlineLineBreak()
                    },
                    text,
                    t -> {
                        InlineText inlineText = new InlineText(text);
                        inlineText.setExtent(text);
                        return inlineText;
                    }).stream().filter(inline -> !(inline instanceof InlineText) || !((InlineText) inline).text.isEmpty())
                    .collect(Collectors.toList());
        }
        
        @Override
        public String toString() {
            return "InlineText{" +
                   "text='" + text + '\'' +
                   '}';
        }
    }
    
    public static class InlineCode extends Inline {
        public final String text;
        
        public InlineCode(String text) {
            this.text = text;
        }
        
        public static Inline inline(JParseDown parseDown, String text, String context) {
            char marker = text.charAt(0);
            Pattern regex = Pattern.compile("^([" + marker + "]++)[ ]*+(.+?)[ ]*+(?<![" + marker + "])\\1(?!" + marker + ")", Pattern.DOTALL);
            Matcher m = regex.matcher(text);
            if (m.find()) {
                text = m.group(2).replaceAll("[ ]*+\\n", " ");
                return new InlineCode(text).setExtent(m.group(0));
            } else
                return null;
        }
        
        @Override
        public String toString() {
            return "InlineCode{" +
                   "text='" + text + '\'' +
                   '}';
        }
    }
    
    public static class InlineEmailTag extends Inline {
        public final String url;
        
        public InlineEmailTag(String url) {
            this.url = url;
        }
        
        public static Inline inline(JParseDown parseDown, String text, String context) {
            if (text.indexOf('>') < 0)
                return null;
            String hostnameLabel = "[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?";
            String commonMarkEmail = "[a-zA-Z0-9.!#$%&\\'*+\\/=?^_`{|}~-]++@"
                                     + hostnameLabel + "(?:\\." + hostnameLabel + ")*";
            
            Matcher m = Pattern.compile("^<((mailto:)?" + commonMarkEmail + ")>", Pattern.CASE_INSENSITIVE).matcher(text);
            if (m.find()) {
                String url = m.group(1);
                return new InlineEmailTag(url).setExtent(m.group(0));
            } else
                return null;
        }
        
        @Override
        public String toString() {
            return "InlineEmailTag{" +
                   "url='" + url + '\'' +
                   '}';
        }
    }
    
    public static Pattern[] strongRegex = {
            Pattern.compile("^[*]{2}((?:\\\\\\*|[^*]|[*][^*]*+[*])+?)[*]{2}(?![*])", Pattern.DOTALL),
            Pattern.compile("^__((?:\\\\_|[^_]|_[^_]*+_)+?)__(?!_)", Pattern.DOTALL | Pattern.UNICODE_CHARACTER_CLASS),
    };
    
    public static Pattern[] emRegex = {
            Pattern.compile("^[*]((?:\\\\\\*|[^*]|[*][*][^*]+?[*][*])+?)[*](?![*])", Pattern.DOTALL),
            Pattern.compile("^_((?:\\\\_|[^_]|__[^_]*__)+?)_(?!_)\\b", Pattern.DOTALL | Pattern.UNICODE_CHARACTER_CLASS),
    };
    
    public static class InlineBold extends Inline {
        public final String text;
        
        public InlineBold(String text) {
            this.text = text;
        }
        
        public static Inline inline(JParseDown parseDown, String text, String context) {
            if (text.length() < 2)
                return null;
            char marker = text.charAt(0);
            int markerIndex = marker == '*' ? 0 : 1;
            
            Matcher m;
            if (text.charAt(1) == marker && (m = strongRegex[markerIndex].matcher(text)).find())
                return new InlineBold(m.group(1)).setExtent(m.group(0));
            else if ((m = emRegex[markerIndex].matcher(text)).find())
                return new InlineItalic(m.group(1)).setExtent(m.group(0));
            else
                return null;
        }
        
        @Override
        public String toString() {
            return "InlineBold{" +
                   "text='" + text + '\'' +
                   '}';
        }
    }
    
    public static class InlineItalic extends Inline {
        public final String text;
        
        public InlineItalic(String text) {
            this.text = text;
        }
        
        @Override
        public String toString() {
            return "InlineItalic{" +
                   "text='" + text + '\'' +
                   '}';
        }
    }
    
    public static String specialCharacters = "\\`*_{}[]()>#+-.!|~";
    
    public static class InlineEscapeSequence extends Inline {
        public final String rawHtml;
        
        public InlineEscapeSequence(String rawHtml) {
            this.rawHtml = rawHtml;
        }
        
        public static Inline inline(JParseDown parseDown, String text, String context) {
            if (text.length() > 1 && specialCharacters.indexOf(text.charAt(1)) >= 0) {
                return new InlineEscapeSequence(Character.toString(text.charAt(1))).setExtent(2);
            } else
                return null;
        }
        
        @Override
        public String toString() {
            return "InlineEscapeSequence{" +
                   "rawHtml='" + rawHtml + '\'' +
                   '}';
        }
    }
    
    public static class InlineImage extends Inline {
        public final String src;
        public final String alternateText;
        
        public InlineImage(String src, String alternateText) {
            this.src = src;
            this.alternateText = alternateText;
        }
        
        public static Inline inline(JParseDown parseDown, String text, String context) {
            if (text.length() < 2 || text.charAt(1) != '[')
                return null;
            text = text.substring(1);
            
            InlineLink link = (InlineLink) InlineLink.inline(parseDown, text, context);
            if (link == null)
                return null;
    
            return new InlineImage(link.url, link.text).setExtent(link.extent + 1);
        }
        
        @Override
        public String toString() {
            return "InlineImage{" +
                   "src='" + src + '\'' +
                   ", alt='" + alternateText + '\'' +
                   '}';
        }
    }
    
    public static class InlineLink extends Inline {
        public final String text;
        public final String url;
        public final String title;
        
        public InlineLink(String text, String url, String title) {
            this.text = text;
            this.url = url;
            this.title = title;
        }
        
        public static Inline inline(JParseDown parseDown, String text, String context) {
            String elementText;
            String url;
            String title = null;
            
            int extent = 0;
            String remainder = text;
            
            Matcher m;
            // Parsedown original pattern: "\\[((?:[^][]++|(?R))*+)\\]" (does not compile in Java)
            if ((m = Pattern.compile("\\[((?:\\\\.|[^\\[\\]]|!\\[[^\\[\\]]*\\])*)\\]").matcher(remainder)).find()) {
                elementText = m.group(1);
                extent += m.group(0).length();
                remainder = remainder.substring(extent);
            } else
                return null;
            
            if ((m = Pattern.compile("^[(]\\s*+((?:[^ ()]++|[(][^ )]+[)])++)(?:[ ]+(\"[^\"]*+\"|\\'[^\\']*+\'))?\\s*+[)]").matcher(remainder)).find()) {
                url = parseDown.convertUrl(m.group(1));
                if (m.group(2) != null)
                    title = m.group(2).substring(1, m.group(2).length() - 1);
                extent += m.group(0).length();
            } else {
                String definition;
                if ((m = Pattern.compile("^\\s*\\[(.*?)\\]").matcher(remainder)).find()) {
                    definition = !m.group(1).isEmpty() ? m.group(1) : elementText;
                    definition = definition.toLowerCase();
                    extent += m.group(0).length();
                } else {
                    definition = elementText.toLowerCase();
                }
                
                ReferenceData reference = parseDown.referenceDefinitions.get(definition);
                if (reference == null)
                    return null;
                url = reference.url;
                title = reference.title;
            }
            
            Inline inline = new InlineLink(elementText, url, title).setExtent(extent);
            inline.nonNestables.add(InlineUrl.class);
            inline.nonNestables.add(InlineLink.class);
            return inline;
        }
        
        @Override
        public String toString() {
            return "InlineLink{" +
                   "text='" + text + '\'' +
                   ", url='" + url + '\'' +
                   ", title='" + title + '\'' +
                   '}';
        }
    }
    
    public static class InlineMarkup extends Inline {
        public final String rawHtml;
        
        public InlineMarkup(String rawHtml) {
            this.rawHtml = rawHtml;
        }
        
        public static Inline inline(JParseDown parseDown, String text, String context) {
            if (parseDown.markupEscaped || parseDown.safeMode || text.indexOf('>') < 0)
                return null;
            
            Matcher m;
            if (text.charAt(1) == '/' && (m = Pattern.compile("^<\\/\\w[\\w-]*+[ ]*+>", Pattern.DOTALL).matcher(text)).find()) {
                return new InlineMarkup(m.group(0)).setExtent(m.group(0));
            }
            if (text.charAt(1) == '!' && (m = Pattern.compile("^<!---?[^>-](?:-?+[^-])*-->", Pattern.DOTALL).matcher(text)).find()) {
                return new InlineMarkup(m.group(0)).setExtent(m.group(0));
            }
            if (text.charAt(1) != ' ' && (m = Pattern.compile("^<\\w[\\w-]*+(?:[ ]*+" + regexHtmlAttribute + ")*+[ ]*+\\/?>", Pattern.DOTALL).matcher(text)).find()) {
                return new InlineMarkup(m.group(0)).setExtent(m.group(0));
            }
            return null;
        }
        
        @Override
        public String toString() {
            return "InlineMarkup{" +
                   "rawHtml='" + rawHtml + '\'' +
                   '}';
        }
    }
    
    public static class InlineSpecialCharacter extends Inline {
        public final String rawHtml;
        
        public InlineSpecialCharacter(String rawHtml) {
            this.rawHtml = rawHtml;
        }
        
        public static Inline inline(JParseDown parseDown, String text, String context) {
            Matcher m;
            if (text.length() > 1 && text.charAt(1) != ' ' && text.indexOf(';') >= 0 &&
                (m = Pattern.compile("^&(#?+[0-9a-zA-Z]++);").matcher(text)).find()) {
                return new InlineSpecialCharacter("&" + m.group(1) + ";").setExtent(m.group(0));
            } else
                return null;
        }
        
        @Override
        public String toString() {
            return "InlineSpecialCharacter{" +
                   "rawHtml='" + rawHtml + '\'' +
                   '}';
        }
    }
    
    public static class InlineStrikeThrough extends Inline {
        public final String text;
        
        public InlineStrikeThrough(String text) {
            this.text = text;
        }
        
        public static Inline inline(JParseDown parseDown, String text, String context) {
            if (text.length() < 2)
                return null;
            Matcher m;
            if (text.charAt(1) == '~' && (m = Pattern.compile("^~~(?=\\S)(.+?)(?<=\\S)~~").matcher(text)).find()) {
                return new InlineStrikeThrough(m.group(1)).setExtent(m.group(0));
            } else
                return null;
        }
        
        @Override
        public String toString() {
            return "InlineStrikeThrough{" +
                   "text='" + text + '\'' +
                   '}';
        }
    }
    
    public static class InlineUrl {
        public static Inline inline(JParseDown parseDown, String text, String context) {
            if (!parseDown.urlsLinked || text.length() < 3 || text.charAt(2) != '/')
                return null;
            Matcher m;
            if (context.contains("http") && (m = Pattern.compile("\\bhttps?:[\\/]{2}[^\\s<]+\\b\\/*",
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS).matcher(context)).find()) {
                String url = parseDown.convertUrl(m.group(0));
                Inline inline = new InlineLink(url, url, null).setExtent(url);
                inline.position = m.start(0);
                return inline;
            } else
                return null;
        }
    }
    
    public static class InlineUrlTag {
        public static Inline inline(JParseDown parseDown, String text, String context) {
            Matcher m;
            if (text.indexOf('>') >= 0 && (m = Pattern.compile("^<(\\w+:\\/{2}[^ >]+)>", Pattern.DOTALL).matcher(text)).find()) {
                String url = parseDown.convertUrl(m.group(1));
                return new InlineLink(url, url, null).setExtent(m.group(0));
            } else {
                return null;
            }
        }
    }
    
    public boolean breaksEnabled = false;
    public boolean markupEscaped = false;
    public boolean urlsLinked = true;
    public boolean safeMode = false;
    public boolean strictMode = false;
    
    public String mdUrlReplacement = null;
    
    public HashMap<String, ReferenceData> referenceDefinitions = new HashMap<>();
    
    public LinkedList<Block> textElements(String text) {
        text = text.replaceAll("\\r\\n?", "\n");
        text = text.replaceAll("(^\\n+)|(\\n+$)", "");
        String[] lines = text.split("\n");
        return this.linesElements(lines);
    }
    
    public JParseDown setBreaksEnabled(boolean breaksEnabled) {
        this.breaksEnabled = breaksEnabled;
        return this;
    }
    
    public JParseDown setMarkupEscaped(boolean markupEscaped) {
        this.markupEscaped = markupEscaped;
        return this;
    }
    
    public JParseDown setUrlsLinked(boolean urlsLinked) {
        this.urlsLinked = urlsLinked;
        return this;
    }
    
    public JParseDown setSafeMode(boolean safeMode) {
        this.safeMode = safeMode;
        return this;
    }
    
    public JParseDown setStrictMode(boolean strictMode) {
        this.strictMode = strictMode;
        return this;
    }
    
    public JParseDown setMdUrlReplacement(String replacement) {
        this.mdUrlReplacement = replacement;
        return this;
    }
    
    public void getBlockTypes(char marker, LinkedList<BlockType<?>> types) {
        switch (marker) {
            case '#':
                types.add(BlockHeader::startBlock);
                return;
            case '+':
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                types.add(BlockList::startBlock);
                return;
            // case '*':
            case '-':
                types.add(BlockRule::startBlock);
                types.add(BlockList::startBlock);
                return;
            case '>':
                types.add(BlockQuote::startBlock);
                return;
            case '[':
                types.add(BlockReference::startBlock);
                return;
            case '_':
                types.add(BlockRule::startBlock);
                return;
            case '`':
            case '~':
                types.add(BlockFencedCode::startBlock);
                return;
        }
    }
    
    public void getUnmarkedBlockTypes(LinkedList<BlockType<?>> types) {
        types.add(BlockCode::startBlock);
    }
    
    public LinkedList<Block> linesElements(LinkedList<String> lines) {
        return linesElements(lines.toArray(new String[0]));
    }
    
    public LinkedList<Block> linesElements(String[] lines) {
        LinkedList<Block> elements = new LinkedList<>();
        Block currentBlock = null;
        
        line:
        for (String line : lines) {
            if (line.trim().isEmpty()) {
                if (currentBlock != null)
                    currentBlock.interrupted++;
                continue;
            }
            
            int tabIndex;
            while ((tabIndex = line.indexOf('\t')) >= 0) {
                int shortage = 4 - tabIndex % 4;
                StringBuilder sb = new StringBuilder();
                sb.append(line.substring(0, tabIndex));
                for (int i = 0; i < shortage; i++)
                    sb.append(' ');
                sb.append(line.substring(tabIndex + 1));
                line = sb.toString();
            }
            
            Line lineObj = new Line(line);
            
            if (currentBlock != null && currentBlock.isContinuable()) {
                Block block = currentBlock.continueBlock(lineObj);
                if (block != null) {
                    currentBlock = block;
                    continue;
                } else if (currentBlock.isCompletable()) {
                    currentBlock = currentBlock.completeBlock();
                }
            }
            
            LinkedList<BlockType<?>> blockTypes = new LinkedList<>();
            getUnmarkedBlockTypes(blockTypes);
            getBlockTypes(lineObj.text.charAt(0), blockTypes);
            
            for (BlockType<?> blockType : blockTypes) {
                Block block = blockType.startBlock(this, lineObj, currentBlock);
                if (block != null) {
                    if (!block.identified) {
                        if (currentBlock != null) {
                            elements.add(currentBlock);
                        }
                        block.identified = true;
                    }
                    currentBlock = block;
                    continue line;
                }
            }
            
            Block block = null;
            if (currentBlock instanceof BlockParagraph) {
                block = currentBlock.continueBlock(lineObj);
            }
            
            if (block != null) {
                currentBlock = block;
            } else {
                if (currentBlock != null) {
                    elements.add(currentBlock);
                }
                currentBlock = BlockParagraph.startBlock(this, lineObj, null);
                currentBlock.identified = true;
            }
        }
        
        if (currentBlock != null && currentBlock.isContinuable() && currentBlock.isCompletable()) {
            currentBlock = currentBlock.completeBlock();
        }
        if (currentBlock != null) {
            elements.add(currentBlock);
        }
        
        for (Block element : elements) {
            element.inlines = new LinkedList<>(element.inline(this));
            if (element.autoBreak == null)
                element.autoBreak = false;
        }
        
        return elements;
    }
    
    public InlineType<?>[] getInlineTypes(char marker) {
        switch (marker) {
            case '!':
                return new InlineType[]{InlineImage::inline};
            case '&':
                return new InlineType[]{InlineSpecialCharacter::inline};
            case '*':
                return new InlineType[]{InlineBold::inline};
            case ':':
                return new InlineType[]{InlineUrl::inline};
            case '<':
                return new InlineType[]{InlineUrlTag::inline, InlineEmailTag::inline, InlineMarkup::inline};
            case '[':
                return new InlineType[]{InlineLink::inline};
            case '_':
                return new InlineType[]{InlineBold::inline};
            case '`':
                return new InlineType[]{InlineCode::inline};
            case '~':
                return new InlineType[]{InlineStrikeThrough::inline};
            case '\\':
                return new InlineType[]{(parseDown, text, context) -> {
                    if (text.length() >= 2 && text.charAt(0) == '\\' && text.charAt(1) == 'n') {
                        return new InlineLineBreak().setExtent(2);
                    }
                    return null;
                }};
            default:
                return new InlineType[]{};
        }
    }
    
    public Pattern inlineMarkerList = Pattern.compile("[!\\*_&\\[:<`~\\\\]");
    
    public LinkedList<Inline> lineElements(String text, HashSet<Class<?>> nonNestables) {
        text = text.replaceAll("\\r\\n?", "\n");
        LinkedList<Inline> elements = new LinkedList<>();
        if (nonNestables == null)
            nonNestables = new HashSet<>();
        
        text:
        for (; ; ) {
            Matcher m = inlineMarkerList.matcher(text);
            if (!m.find())
                break;
            int markerPosition = m.start();
            String excerpt = text.substring(markerPosition);
            
            for (InlineType<?> inlineType : getInlineTypes(excerpt.charAt(0))) {
                if (nonNestables.contains(inlineType.getClass()))
                    continue;
                Inline inline = inlineType.inline(this, excerpt, text);
                if (inline == null)
                    continue;
                
                if (inline.position >= 0 && inline.position > markerPosition)
                    continue;
                if (inline.position < 0)
                    inline.position = markerPosition;
                
                inline.nonNestables.addAll(nonNestables);
                
                String unmarkedText = text.substring(0, inline.position);
                elements.addAll(InlineText.inline(this, unmarkedText, null));
                
                elements.add(inline);
                
                text = text.substring(inline.position + inline.extent);
                continue text;
            }
            
            String unmarkedText = text.substring(0, markerPosition + 1);
            elements.addAll(InlineText.inline(this, unmarkedText, null));
            
            text = text.substring(markerPosition + 1);
        }
        
        elements.addAll(InlineText.inline(this, text, null));
        
        return elements;
    }
    
    public String convertUrl(String url) {
        if (mdUrlReplacement == null || url.indexOf(':') >= 0)
            return url;
        Matcher m = Pattern.compile("(\\.md)(#.*)?$").matcher(url);
        if (m.find())
            return m.replaceFirst(mdUrlReplacement + "$2");
        else
            return url;
    }
    
    public static <C extends Component> LinkedList<C> replaceAllElements(String regex, C[] elements, String text, Function<String, C> elementFactory) {
        LinkedList<C> newElements = new LinkedList<>();
        Matcher m = Pattern.compile(regex).matcher(text);
        int end = 0;
        while (m.find()) {
            String before = text.substring(end, m.start());
            newElements.add(elementFactory.apply(before));
            Collections.addAll(newElements, elements);
            end = m.end();
        }
        newElements.add(elementFactory.apply(text.substring(end)));
        return newElements;
    }
    
    public static int startSpan(String s, char c) {
        int i = 0;
        int len = s.length();
        while (i < len && s.charAt(i) == c) i++;
        return i;
    }
}