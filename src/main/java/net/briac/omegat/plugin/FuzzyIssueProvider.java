package net.briac.omegat.plugin;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.omegat.core.CoreEvents;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.gui.issues.IIssue;
import org.omegat.gui.issues.IIssueProvider;
import org.omegat.gui.issues.IssueDetailSplitPanel;
import org.omegat.gui.issues.IssueProviders;
import org.omegat.gui.issues.SimpleIssue;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;

public class FuzzyIssueProvider implements IIssueProvider {
    protected static final ResourceBundle res = ResourceBundle.getBundle("fuzzy-issues", Locale.getDefault());

    public static void loadPlugins() {
        CoreEvents.registerApplicationEventListener(new IApplicationEventListener() {
            @Override
            public void onApplicationStartup() {
                IssueProviders.addIssueProvider(new FuzzyIssueProvider());
            }

            @Override
            public void onApplicationShutdown() {
                /* empty */
            }
        });
    }

    public static void unloadPlugins() {
        /* empty */
    }

    @Override
    public List<IIssue> getIssues(SourceTextEntry sourceEntry, TMXEntry tmxEntry) {
        String prefix = Preferences.getPreferenceDefault(Preferences.BEST_MATCH_EXPLANATORY_TEXT,
                OStrings.getString("WF_DEFAULT_PREFIX"));

        if (tmxEntry.isTranslated() && tmxEntry.translation.startsWith(prefix)) {
            List<IIssue> issues = new ArrayList<>(1);
            issues.add(new FuzzyIssue(sourceEntry, tmxEntry));
            return issues;
        }

        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return res.getString("FUZZY_ISSUES_PROVIDER_NAME");
    }

    @Override
    public String getId() {
        return getClass().getCanonicalName();
    }

    private static class FuzzyIssue extends SimpleIssue {
        private static final String FUZZY_ISSUE_COLOR = "#f9df16";
        private static final AttributeSet ERROR_STYLE;
        static {
            SimpleAttributeSet attr = new SimpleAttributeSet();
            StyleConstants.setForeground(attr, Color.decode(FUZZY_ISSUE_COLOR));
            StyleConstants.setBold(attr, true);
            ERROR_STYLE = attr;
        }

        private SourceTextEntry ste;
        private TMXEntry tmxEntry;

        public FuzzyIssue(SourceTextEntry sourceEntry, TMXEntry targetEntry) {
            super(sourceEntry, targetEntry);
            ste = sourceEntry;
            tmxEntry = targetEntry;
        }

        @Override
        public String getTypeName() {
            return res.getString("FUZZY_ISSUE_NAME");
        }

        @Override
        public String getDescription() {
            return res.getString("FUZZY_ISSUE_DESCRIPTION");
        }

        @Override
        protected String getColor() {
            return FUZZY_ISSUE_COLOR;
        }

        @Override
        public Component getDetailComponent() {
            IssueDetailSplitPanel panel = new IssueDetailSplitPanel();
            panel.firstTextPane.setText(ste.getSrcText());
            panel.lastTextPane.setText(tmxEntry.translation);
            StyledDocument doc = panel.lastTextPane.getStyledDocument();

            String prefix = Preferences.getPreferenceDefault(Preferences.BEST_MATCH_EXPLANATORY_TEXT,
                    OStrings.getString("WF_DEFAULT_PREFIX"));
            
            doc.setCharacterAttributes(0, prefix.length(), ERROR_STYLE, false);

            panel.setMinimumSize(new Dimension(0, panel.firstTextPane.getFont().getSize() * 6));
            return panel;
        }

    }
}
