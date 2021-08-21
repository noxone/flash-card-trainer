package org.olafneumann.chinese;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class PinyinDocument extends PlainDocument {
	private static final long serialVersionUID = -3871872899907677791L;

	private static final String[] SYLLABLES = { "a", "ai", "an", "ang", "ao", "ba", "bai", "ban", "bang", "bao", "bei", "ben", "beng",
			"bi", "bian", "biao", "bie", "bin", "bing", "bo", "bu", "ca", "cai", "can", "cang", "cao", "ce", "cen", "ceng", "cha", "chai",
			"chan", "chang", "chao", "che", "chen", "cheng", "chi", "chong", "chou", "chu", "chua", "chuai", "chuan", "chuang", "chui",
			"chun", "chuo", "ci", "cong", "cou", "cu", "cuan", "cui", "cun", "cuo", "da", "dai", "dan", "dang", "dao", "de", "dei", "den",
			"deng", "di", "dia", "dian", "diao", "die", "ding", "diu", "dong", "dou", "du", "duan", "dui", "dun", "duo", "e", "ei", "en",
			"eng", "er", "fa", "fan", "fang", "fei", "fen", "feng", "fo", "fou", "fu", "ga", "gai", "gan", "gang", "gao", "ge", "gei",
			"gen", "geng", "gong", "gou", "gu", "gua", "guai", "guan", "guang", "gui", "gun", "guo", "ha", "hai", "han", "hang", "hao",
			"he", "hei", "hen", "heng", "hong", "hou", "hu", "hua", "huai", "huan", "huang", "hui", "hun", "huo", "ji", "jia", "jian",
			"jiang", "jiao", "jie", "jin", "jing", "jiong", "jiu", "ju", "juan", "jue", "jun", "ka", "kai", "kan", "kang", "kao", "ke",
			"ken", "keng", "kong", "kou", "ku", "kua", "kuai", "kuan", "kuang", "kui", "kun", "kuo", "la", "lai", "lan", "lang", "lao",
			"le", "lei", "leng", "li", "lia", "lian", "liang", "liao", "lie", "lin", "ling", "liu", "lo", "long", "lou", "lu", "luan",
			"lun", "luo", "lü", "lüe", "ma", "mai", "man", "mang", "mao", "me", "mei", "men", "meng", "mi", "mian", "miao", "mie", "min",
			"ming", "miu", "mo", "mou", "mu", "na", "nai", "nan", "nang", "nao", "ne", "nei", "nen", "neng", "ni", "nian", "niang", "niao",
			"nie", "nin", "ning", "niu", "nong", "nou", "nu", "nuan", "nun", "nuo", "nü", "nüe", "o", "ou", "pa", "pai", "pan", "pang",
			"pao", "pei", "pen", "peng", "pi", "pian", "piao", "pie", "pin", "ping", "po", "pou", "pu", "qi", "qia", "qian", "qiang",
			"qiao", "qie", "qin", "qing", "qiong", "qiu", "qu", "quan", "que", "qun", "ran", "rang", "rao", "re", "ren", "reng", "ri",
			"rong", "rou", "ru", "rua", "ruan", "rui", "run", "ruo", "sa", "sai", "san", "sang", "sao", "se", "sen", "seng", "sha", "shai",
			"shan", "shang", "shao", "she", "shei", "shen", "sheng", "shi", "shou", "shu", "shua", "shuai", "shuan", "shuang", "shui",
			"shun", "shuo", "si", "song", "sou", "su", "suan", "sui", "sun", "suo", "ta", "tai", "tan", "tang", "tao", "te", "tei", "teng",
			"ti", "tian", "tiao", "tie", "ting", "tong", "tou", "tu", "tuan", "tui", "tun", "tuo", "wa", "wai", "wan", "wang", "wei",
			"wen", "weng", "wo", "wu", "xi", "xia", "xian", "xiang", "xiao", "xie", "xin", "xing", "xiong", "xiu", "xu", "xuan", "xue",
			"xun", "ya", "yai", "yan", "yang", "yao", "ye", "yi", "yin", "ying", "yo", "yong", "you", "yu", "yuan", "yue", "yun", "za",
			"zai", "zan", "zang", "zao", "ze", "zei", "zen", "zeng", "zha", "zhai", "zhan", "zhang", "zhao", "zhe", "zhei", "zhen",
			"zheng", "zhi", "zhong", "zhou", "zhu", "zhua", "zhuai", "zhuan", "zhuang", "zhui", "zhun", "zhuo", "zi", "zong", "zou", "zu",
			"zuan", "zui", "zun", "zuo" };

	private static final int MAX_SYLLABLE_LENGTH;

	static {
		int length = 0;
		for (String syllable : SYLLABLES)
			if (syllable.length() > length)
				length = syllable.length();
		MAX_SYLLABLE_LENGTH = length;
	}

	private static final String A = "aāáǎà";
	private static final String E = "eēéěè";
	private static final String I = "iīíǐì";
	private static final String O = "oōóǒò";
	private static final String U = "uūúǔù";
	private static final String Ü = "üǖǘǚǜ";
	private static final String[] VOCALES = { A, E, I, O, U, Ü };

	private static final String VOCALE = "aeiouü";
	private static final String VOCALE_PATTERN_STRING = "[" + VOCALE + "]+";
	private static final Pattern VOCALE_PATTERN = Pattern.compile(VOCALE_PATTERN_STRING);

	private static final Pattern SYLLABLE_WITH_TONE_PATTERN;

	static {
		String pattern = null;
		for (String syllable : SYLLABLES) {
			if (pattern != null)
				pattern += "|";
			if (pattern == null)
				pattern = "";
			pattern += syllable;
		}
		pattern = "(" + pattern + ")([1-4])";
		SYLLABLE_WITH_TONE_PATTERN = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
	}

	private static final Comparator<String> LENGTH_COMPARATOR = new Comparator<String>() {
		@Override
		public int compare(String arg0, String arg1) {
			if (arg0.length() < arg1.length())
				return 1;
			else if (arg0.length() > arg1.length())
				return -1;
			else
				return 0;
		}
	};

	@Override
	public void insertString(int offset, String str, AttributeSet attributeSet) throws BadLocationException {
		if (str != null) {
			if (str.length() == 1) {
				if (isTone(str.charAt(0))) {
					String syllable = getSyllabelBeforeOffset(offset);
					if (syllable != null) {
						String originalText = getText(offset - syllable.length(), syllable.length());
						String newSyllable = makeTone(syllable, getTone(str.charAt(0)));
						if (originalText.length() == newSyllable.length()) {
							StringBuilder sb = new StringBuilder(newSyllable.length());
							for (int i = 0; i < originalText.length(); ++i) {
								char oc = originalText.charAt(i);
								char nc = newSyllable.charAt(i);
								if (Character.isUpperCase(oc))
									nc = Character.toUpperCase(nc);
								sb.append(nc);
							}
							newSyllable = sb.toString();
						}
						if (!originalText.equals(newSyllable)) {
							replace(offset - syllable.length(), syllable.length(), newSyllable, attributeSet);
							return;
						}
					}
				}
			} else {
				// TODO do replacements in another order to improve undo possibilities
				
				// Find first tone indicator
				int firstTone = -1;
				for (char t = '1'; t <= '4'; ++t) {
					int pos = str.indexOf(t);
					if (pos != -1 && pos > firstTone)
						firstTone = pos;
				}

				if (firstTone != -1) {
					int addedCharacters = 0;
					String input = str;
					if (firstTone < MAX_SYLLABLE_LENGTH) {
						int neededCharacters = MAX_SYLLABLE_LENGTH - firstTone;
						addedCharacters = Math.min(offset, neededCharacters);
						input = getText(offset - addedCharacters, addedCharacters) + input;
					}

					// find syllables
					Matcher matcher = SYLLABLE_WITH_TONE_PATTERN.matcher(input);
					Map<String, String> replacingSyllables = new HashMap<String, String>();
					int usedCharacters = 0;
					while (matcher.find()) {
						if (matcher.start() < addedCharacters) {
							usedCharacters = addedCharacters - matcher.start();
						}
						String syllable = matcher.group(1);
						String toneString = matcher.group(2);
						if (isTone(toneString.charAt(0))) {
							replacingSyllables.put(matcher.group(), makeTone(syllable, getTone(toneString.charAt(0))));
						}
					}

					if (!replacingSyllables.isEmpty()) {
						// replace found syllables
						for (Entry<String, String> entry : replacingSyllables.entrySet()) {
							input = input.replaceAll(entry.getKey(), entry.getValue());
						}

						// remove unused characters, that we've added
						input = input.substring(addedCharacters - usedCharacters);

						// if we used other characters, replace them...
						if (usedCharacters > 0) {
							String characters;
							if (input.length() >= usedCharacters) {
								characters = input.substring(0, usedCharacters);
								input = input.substring(usedCharacters);
							} else {
								characters = input;
								input = "";
							}
							replace(offset - usedCharacters, usedCharacters, characters, attributeSet);
						}

						// insert input
						super.insertString(offset, input, attributeSet);
						return;
					}
				}
			}
		}
		super.insertString(offset, str, attributeSet);
	}

	private boolean isTone(char c) {
		return c == '1' || c == '2' || c == '3' || c == '4';
	}

	private int getTone(char c) {
		return c - '0';
	}

	private String clearSyllable(String syllable) {
		for (String vocale : VOCALES) {
			for (int i = 1; i < vocale.length(); ++i) {
				syllable = syllable.replace(vocale.charAt(i), vocale.charAt(0));
			}
		}
		return syllable;
	}

	private String makeTone(String syllable, int tone) {
		Matcher matcher = VOCALE_PATTERN.matcher(syllable);
		if (matcher.find()) {
			String vocales = matcher.group();
			for (char c : VOCALE.toCharArray()) {
				if (vocales.contains("" + c)) {
					vocales = vocales.replaceFirst("" + c, "" + makeTone(c, tone));
					return syllable.substring(0, matcher.start()) + vocales + syllable.substring(matcher.end());
				}
			}
		}
		return syllable;
	}

	private char makeTone(char vocale, int tone) {
		switch (vocale) {
			case 'a':
				return A.charAt(tone);
			case 'e':
				return E.charAt(tone);
			case 'i':
				return I.charAt(tone);
			case 'o':
				return O.charAt(tone);
			case 'u':
				return U.charAt(tone);
			case 'ü':
				return Ü.charAt(tone);
			default:
				throw new RuntimeException("Unknown vocale: " + vocale);
		}
	}

	private String getSyllabelBeforeOffset(int offset) throws BadLocationException {
		int length = Math.min(Math.min(MAX_SYLLABLE_LENGTH, getLength()), offset);
		String part = getText(offset - length, length);
		return getSyllable(part);
	}

	private String getSyllable(String string) {
		String partLower = clearSyllable(string.toLowerCase());

		Set<String> foundSyllables = new TreeSet<String>(LENGTH_COMPARATOR);
		for (String syllable : SYLLABLES) {
			if (syllable.length() <= string.length()) {
				if (partLower.endsWith(syllable)) {
					foundSyllables.add(syllable);
				}
			}
		}

		if (!foundSyllables.isEmpty())
			return foundSyllables.iterator().next();
		else
			return null;
	}
}
