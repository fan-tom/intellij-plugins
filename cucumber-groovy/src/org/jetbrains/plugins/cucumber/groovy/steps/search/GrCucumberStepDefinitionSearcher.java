package org.jetbrains.plugins.cucumber.groovy.steps.search;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.pom.PomTarget;
import com.intellij.pom.PomTargetPsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
import com.intellij.util.QueryExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberUtil;
import org.jetbrains.plugins.cucumber.groovy.GrCucumberUtil;
import org.jetbrains.plugins.cucumber.groovy.steps.GrStepDefinition;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall;

/**
 * @author Max Medvedev
 */
public class GrCucumberStepDefinitionSearcher implements QueryExecutor<PsiReference, ReferencesSearch.SearchParameters> {
  @Override
  public boolean execute(@NotNull final ReferencesSearch.SearchParameters queryParameters,
                         @NotNull final Processor<PsiReference> consumer) {
    final PsiElement element = ApplicationManager.getApplication().runReadAction(new NullableComputable<PsiElement>() {
      @Override
      public PsiElement compute() {
        return getStepDefinition(queryParameters.getElementToSearch());
      }
    });
    if (element == null) return true;

    @Nullable
    final String regexp = ApplicationManager.getApplication().runReadAction(new NullableComputable<String>() {
      @Nullable
      @Override
      public String compute() {
        return GrCucumberUtil.getStepDefinitionPatternText((GrMethodCall)element);
      }
    });

    return CucumberUtil.findGherkinReferencesToElement(element, regexp, consumer, queryParameters.getEffectiveSearchScope());
  }

  public static PsiElement getStepDefinition(final PsiElement element) {
    if (GrCucumberUtil.isStepDefinition(element)) {
      return element;
    }

    if (element instanceof PomTargetPsiElement) {
      final PomTarget target = ((PomTargetPsiElement)element).getTarget();
      if (target instanceof GrStepDefinition) {
        return ((GrStepDefinition)target).getElement();
      }
    }

    return null;
  }
}
