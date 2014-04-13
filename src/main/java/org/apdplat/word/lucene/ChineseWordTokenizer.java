/**
 * 
 * APDPlat - Application Product Development Platform
 * Copyright (c) 2013, 杨尚川, yang-shangchuan@qq.com
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package org.apdplat.word.lucene;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Queue;
import java.util.concurrent.LinkedTransferQueue;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apdplat.word.WordSeg;
import org.apdplat.word.segmentation.Word;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lucene中文分词器
 * @author 杨尚川
 */
public class ChineseWordTokenizer extends Tokenizer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChineseWordTokenizer.class);
    
    private final CharTermAttribute charTermAttribute = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAttribute = addAttribute(OffsetAttribute.class);
    private final PositionIncrementAttribute positionIncrementAttribute = addAttribute(PositionIncrementAttribute.class);
    
    private BufferedReader reader = null;
    private final Queue<Word> words = new LinkedTransferQueue<>();
    private int startOffset=0;
        
    public ChineseWordTokenizer(Reader input) {
        super(input);
        reader = new BufferedReader(input);
    }
    
    @Override
    public final boolean incrementToken() throws IOException {
        Word word = words.poll();
        if(word == null){
            String line;
            while( (line = reader.readLine()) != null ){
                words.addAll(WordSeg.seg(line));
            }
            startOffset = 0;
            word = words.poll();
        }
        if (word != null) {
            charTermAttribute.setEmpty().append(word.getText());
            offsetAttribute.setOffset(startOffset, startOffset+word.getText().length());
            positionIncrementAttribute.setPositionIncrement(1);
            startOffset += word.getText().length();
            return true;
        }
        return false;
    }
}