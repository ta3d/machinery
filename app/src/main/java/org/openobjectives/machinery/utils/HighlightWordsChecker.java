/**
 * License APACHE-2.0  https://www.apache.org/licenses/LICENSE-2.0.html
 */
package org.openobjectives.machinery.utils;
/**
 * <B>Class: HighlightWordsChecker </B> <br>
 * <B>Class changed: </B> 19.05.2020 <br>
 * **************************************************************************** <br>
 * Class description: A ćhecker if a String is found in a wordlist <br>
 * <br>
 * ***************************************************************************** <br>
 *
 */
import java.util.Collection;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HighlightWordsChecker {

    private static final String TAG = HighlightWordsChecker.class.getSimpleName();
    public final Pattern highWords;

    public HighlightWordsChecker(Collection<String> highWords) {
        this.highWords =
                Pattern.compile(
                        highWords.stream()
                                .map(Pattern::quote)
                                .collect(Collectors.joining("|")));
    }

    public boolean containsHighWords(String string) {
        return highWords.matcher(string).find();
    }
}