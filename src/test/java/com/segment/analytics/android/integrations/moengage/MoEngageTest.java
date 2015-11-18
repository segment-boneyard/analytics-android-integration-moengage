package com.segment.analytics.android.integrations.moengage;

import android.app.Activity;
import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import com.moe.pushlibrary.MoEHelper;
import com.moe.pushlibrary.models.GeoLocation;
import com.moe.pushlibrary.utils.MoEHelperConstants;
import com.segment.analytics.Analytics;
import com.segment.analytics.AnalyticsContext;
import com.segment.analytics.Properties;
import com.segment.analytics.Traits;
import com.segment.analytics.ValueMap;
import com.segment.analytics.core.tests.BuildConfig;
import com.segment.analytics.test.IdentifyPayloadBuilder;
import com.segment.analytics.test.ScreenPayloadBuilder;
import com.segment.analytics.test.TrackPayloadBuilder;
import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static com.segment.analytics.Utils.createContext;
import static com.segment.analytics.Utils.createTraits;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyNoMoreInteractions;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 18, manifest = Config.NONE, application = Application.class)
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*", "org.json.*" })
@PrepareForTest(MoEngageIntegration.class) public class MoEngageTest {

  @Rule public PowerMockRule rule = new PowerMockRule();

  @Mock Application context;
  @Mock Analytics analytics;
  @Mock MoEHelper moeHelper;
  @Mock ApplicationInfo applicationInfo;
  @Mock PackageManager manager;
  @Mock Activity activity;
  MoEngageIntegration integration;

  @Before public void setUp() throws PackageManager.NameNotFoundException {
    initMocks(this);
    mockStatic(MoEHelper.class);
    when(analytics.getApplication()).thenReturn(context);

    when(context.getPackageManager()).thenReturn(manager);
    when(activity.getPackageManager()).thenReturn(manager);
    when(manager.getApplicationInfo(context.getPackageName(), 0)).thenReturn(applicationInfo);

    integration = new MoEngageIntegration(analytics, new ValueMap());
    integration.helper = moeHelper;
  }

  @Test public void initialize() {
    MoEngageIntegration integration = new MoEngageIntegration(analytics, new ValueMap());
    assertThat(integration.helper).isNotNull();
    verifyNoMoreInteractions(MoEHelper.class);
    verifyNoMoreInteractions(moeHelper);
  }

  @Test public void activityCreate() {
    Bundle bundle = mock(Bundle.class);
    integration.onActivityCreated(activity, bundle);
    verifyNoMoreInteractions(MoEHelper.class);
    verifyNoMoreInteractions(moeHelper);
  }

  @Test public void activityStart() {
    integration.onActivityStarted(activity);
    verify(moeHelper).onStart(activity);
    verifyNoMoreInteractions(MoEHelper.class);
    verifyNoMoreInteractions(moeHelper);
  }

  @Test public void activityResume() {
    integration.onActivityResumed(activity);
    verify(moeHelper).onResume(activity);
    verifyNoMoreInteractions(MoEHelper.class);
    verifyNoMoreInteractions(moeHelper);
  }

  @Test public void activityPause() {
    integration.onActivityPaused(activity);
    verify(moeHelper).onPause(activity);
    verifyNoMoreInteractions(MoEHelper.class);
    verifyNoMoreInteractions(moeHelper);
  }

  @Test public void activityStop() {
    integration.onActivityStopped(activity);
    verify(moeHelper).onStop(activity);
    verifyNoMoreInteractions(MoEHelper.class);
    verifyNoMoreInteractions(moeHelper);
  }

  @Test public void activitySaveInstance() {
    Bundle bundle = mock(Bundle.class);
    integration.onActivitySaveInstanceState(activity, bundle);
    verify(moeHelper).onSaveInstanceState(bundle);
    verifyNoMoreInteractions(MoEHelper.class);
    verifyNoMoreInteractions(moeHelper);
  }

  @Test public void track() throws JSONException {
    TrackPayloadBuilder builder = new TrackPayloadBuilder();
    builder.event("foo").properties(new Properties().putCurrency("INR").putPrice(2000.0));
    integration.track(builder.build());

    JSONObject eventProperties = new JSONObject();
    eventProperties.put("currency", "INR");
    eventProperties.put("price", 2000.0);
    verify(moeHelper).trackEvent(eq("foo"), jsonEq(eventProperties));
    verifyNoMoreInteractions(MoEHelper.class);
    verifyNoMoreInteractions(moeHelper);
  }

  @Test public void screen() {
    integration.screen(new ScreenPayloadBuilder().build());
    verifyNoMoreInteractions(MoEHelper.class);
    verifyNoMoreInteractions(moeHelper);
  }

  @Test public void identify() throws ParseException {
    Traits traits = createTraits("foo") //
        .putValue("anonymousId", "bar")
        .putEmail("foo@bar.com")
        .putPhone("123-542-7189")
        .putName("Mr. Prateek")
        .putFirstName("Prateek")
        .putLastName("Srivastava")
        .putGender("male");
    AnalyticsContext analyticsContext = createContext(traits) //
        .putLocation(new AnalyticsContext.Location().putLatitude(10).putLongitude(20));

    integration.identify(new IdentifyPayloadBuilder() //
        .traits(traits).context(analyticsContext).build());

    HashMap<String, Object> userAttributes = new LinkedHashMap<>();
    userAttributes.put("USER_ATTRIBUTE_SEGMENT_ID", "bar");
    userAttributes.put(MoEHelperConstants.USER_ATTRIBUTE_UNIQUE_ID, "foo");
    userAttributes.put(MoEHelperConstants.USER_ATTRIBUTE_USER_EMAIL, "foo@bar.com");
    userAttributes.put(MoEHelperConstants.USER_ATTRIBUTE_USER_MOBILE, "123-542-7189");
    userAttributes.put(MoEHelperConstants.USER_ATTRIBUTE_USER_NAME, "Mr. Prateek");
    userAttributes.put(MoEHelperConstants.USER_ATTRIBUTE_USER_FIRST_NAME, "Prateek");
    userAttributes.put(MoEHelperConstants.USER_ATTRIBUTE_USER_LAST_NAME, "Srivastava");
    userAttributes.put(MoEHelperConstants.USER_ATTRIBUTE_USER_GENDER, "male");
    verify(moeHelper).setUserAttribute(mapEq(userAttributes));
    verify(moeHelper).setUserAttribute(MoEHelperConstants.USER_ATTRIBUTE_USER_LOCATION,
        new GeoLocation(10, 20));

    verifyNoMoreInteractions(moeHelper);
    verifyNoMoreInteractions(MoEHelper.class);
  }

  @Test public void flush() {
    integration.flush();
    verifyNoMoreInteractions(MoEHelper.class);
    verifyNoMoreInteractions(moeHelper);
  }

  @Test public void reset() {
    integration.reset();
    verify(moeHelper).logoutUser();
    verifyNoMoreInteractions(MoEHelper.class);
    verifyNoMoreInteractions(moeHelper);
  }

  public static <K, V> Map<K, V> mapEq(Map<K, V> expected) {
    return argThat(new MapMatcher<>(expected));
  }

  private static class MapMatcher<K, V> extends TypeSafeMatcher<Map<K, V>> {
    private final Map<K, V> expected;

    private MapMatcher(Map<K, V> expected) {
      this.expected = expected;
    }

    @Override public boolean matchesSafely(Map<K, V> map) {
      return expected.equals(map);
    }

    @Override public void describeTo(Description description) {
      description.appendText(expected.toString());
    }
  }

  public static JSONObject jsonEq(JSONObject expected) {
    return argThat(new JSONObjectMatcher(expected));
  }

  private static class JSONObjectMatcher extends TypeSafeMatcher<JSONObject> {
    private final JSONObject expected;

    private JSONObjectMatcher(JSONObject expected) {
      this.expected = expected;
    }

    @Override public boolean matchesSafely(JSONObject jsonObject) {
      // todo: this relies on having the same order
      return expected.toString().equals(jsonObject.toString());
    }

    @Override public void describeTo(Description description) {
      description.appendText(expected.toString());
    }
  }
}
