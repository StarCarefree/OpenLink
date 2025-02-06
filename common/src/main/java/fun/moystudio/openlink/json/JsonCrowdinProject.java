package fun.moystudio.openlink.json;

import java.util.List;

public class JsonCrowdinProject {
    public List<Language> targetLanguages;
    public static class Language {
        public String name, locale;
    }
}
