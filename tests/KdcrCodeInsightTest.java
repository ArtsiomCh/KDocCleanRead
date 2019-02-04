import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;

public class KdcrCodeInsightTest extends LightCodeInsightFixtureTestCase {

  @Override
  protected String getTestDataPath() {
    return "testData";
  }

  public void testEmphasisHighlighting() {
    myFixture.configureByFiles("EmphasisTestData.kt");
    myFixture.checkHighlighting(false, true, false, false);
  }

}
