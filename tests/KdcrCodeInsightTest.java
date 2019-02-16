import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import java.io.File;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.idea.KotlinFileType;
import org.jetbrains.kotlin.idea.kdoc.KDocTemplate.DescriptionBodyTemplate.Kotlin;

public class KdcrCodeInsightTest extends LightCodeInsightFixtureTestCase {

  @Override
  @NotNull
  protected LightProjectDescriptor getProjectDescriptor() {
    return new DefaultLightProjectDescriptor(){
      @Override
      public void configureModule(@NotNull Module module, @NotNull ModifiableRootModel model, @NotNull ContentEntry contentEntry) {
        super.configureModule(module, model, contentEntry);
        PsiTestUtil.addLibrary(module, getBundledKotlinJarPath());
      }

      private String getBundledKotlinJarPath() {
        String jarPath = PathManager.getJarPathForClass(Kotlin.class);
        assert jarPath != null : "Cannot find JAR containing Kotlin classes";
        return jarPath;// + "!/";
      }
    };
  }

  @Override
  protected String getTestDataPath() {
    return "testData";
  }

  public void testEmphasisHighlighting() {
    doInfoTest("fun foo(){ ");
    doJavaTest("class MyClass { ");
    doInfoTest(" /** *<info descr=\"MY_ITALIC\">1</info>* */");
    doInfoTest(" /** **<info descr=\"MY_BOLD\">2</info>** */");
    doInfoTest(" /** *<info descr=\"MY_ITALIC\">**<info descr=\"MY_BOLD\">3</info>**</info>* */ ");
  }

  /** @return highlighting duration in milliseconds */
  private long doInfoTest(@NotNull String testingText) {
//    myFixture.configureByFiles("EmphasisTestData.kt");
    myFixture.configureByText(KotlinFileType.INSTANCE, testingText);
    PsiDocumentManager.getInstance(getProject()).commitAllDocuments();
    return myFixture.checkHighlighting(false, true, false, false);
  }

  private long doJavaTest(@NotNull String testingText) {
    myFixture.configureByText("myTmpFile.java", testingText);
    return myFixture.checkHighlighting(false, true, false, false);
  }
}
