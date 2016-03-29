package cc.softwarefactory.lokki.android.espresso;

import android.content.Context;
import android.provider.ContactsContract;
import android.support.test.espresso.action.ViewActions;
import android.test.mock.MockContentProvider;
import android.test.mock.MockContentResolver;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.json.JSONException;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import cc.softwarefactory.lokki.android.R;
import cc.softwarefactory.lokki.android.espresso.utilities.MockJsonUtils;
import cc.softwarefactory.lokki.android.espresso.utilities.RequestsHandle;
import cc.softwarefactory.lokki.android.espresso.utilities.TestUtils;
import cc.softwarefactory.lokki.android.fragments.AddContactsFragment;
import cc.softwarefactory.lokki.android.models.Contact;
import cc.softwarefactory.lokki.android.services.ContactService;
import cc.softwarefactory.lokki.android.utilities.JsonUtils;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

public class AddContactsScreenTest extends LoggedInBaseTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        getMockDispatcher().setGetContactsResponse(new MockResponse().setResponseCode(200));
        setMockContacts();
        enterContactsScreen();
    }

    private void setMockContacts() throws IOException, JSONException {
        List<Contact> mockPhoneContacts = JsonUtils.createListFromJson(MockJsonUtils.getContactsJson(), Contact.class);
        getActivity().setPhoneContacts(mockPhoneContacts);
    }

    private void enterContactsScreen() {
        TestUtils.toggleNavigationDrawer();
        onView(withText(R.string.contacts)).perform(click());
    }

    private void enterAddContactsScreen() {
        onView(withId(R.id.add_contacts)).perform(click());
    }

    private void openAddContactDialog() {
        onView(withId(R.id.add_email)).perform(click());
    }

    private void addContactFromContactListScreen(String clickableText, String email) {
        onView(withText(clickableText)).perform(click());

        String message = getResources().getString(R.string.add_contact_dialog_save, email);
        onView(withText(message)).check(matches(isDisplayed()));

        onView(withText(R.string.ok)).perform(click());

        onView(withText(clickableText)).check(doesNotExist());
    }

    // TEST

    public void testContactListScreenIsDisplayed() {
        onView(withText(R.string.can_see_me)).check(matches(isDisplayed()));
        onView(withText(R.string.i_can_see)).check(matches(isDisplayed()));
    }

    public void testOpenAddContactsScreen() {
        enterAddContactsScreen();
        openAddContactDialog();
        onView(withText(R.string.add_contact)).check(matches(isDisplayed()));
        onView(withHint(R.string.contact_email_address)).check(matches(isDisplayed()));
        onView(withText(R.string.ok)).check(matches(isDisplayed()));
        onView(withText(R.string.cancel)).check(matches(isDisplayed()));
    }

    public void testSeeAnyContactOnAddScreen() {
        onView(withId(R.id.add_contacts)).perform(click());
        onView(withText("Family Member")).check(matches(isDisplayed()));
    }

    public void testAddingSingleContact() {
        enterAddContactsScreen();

        String contactName = "Family Member";
        String contactEmail = "family.member@example.com";
        addContactFromContactListScreen(contactName, contactEmail);

    }

    public void testCancelingSingleContactAdding() {
        enterAddContactsScreen();

        String contactEmail = "family.member@example.com";

        onView(withText(contactEmail)).perform(click());
        onView(withText(R.string.cancel)).perform(click());

        onView(withText(contactEmail)).check(matches(isDisplayed()));
    }

    public void testAddingTwoContactsUsingName() {
        enterAddContactsScreen();

        String firstContactName = "Test Friend";
        String firstContactEmail = "test.friend@example.com";
        String secondContactName = "Family Member";
        String secondContactEmail = "family.member@example.com";

        addContactFromContactListScreen(firstContactName, firstContactEmail);
        addContactFromContactListScreen(secondContactName, secondContactEmail);
    }

    public void testAddingTwoContactsUsingEmail() {
        enterAddContactsScreen();

        String firstContactEmail = "test.friend@example.com";
        String secondContactEmail = "family.member@example.com";

        addContactFromContactListScreen(firstContactEmail, firstContactEmail);
        addContactFromContactListScreen(secondContactEmail, secondContactEmail);
    }

    public void testAddingEmptyContact() {
        enterAddContactsScreen();
        openAddContactDialog();
        onView(withText(R.string.ok)).perform(click());
        onView(withHint(R.string.contact_email_address)).check(matches(isDisplayed()));
    }

    public void testAddingCustomContact() {
        enterAddContactsScreen();
        openAddContactDialog();
        onView(withHint(R.string.contact_email_address)).perform(typeText("test@example.com"));
        onView(withText(R.string.ok)).perform(click());
    }

    public void testBackButtonToContactsScreen() {
        onView(withId(R.id.add_contacts)).perform(click());
        onView(isRoot()).perform(ViewActions.pressBack());

        onView(withText(R.string.can_see_me)).check(matches(isDisplayed()));
        onView(withText(R.string.i_can_see)).check(matches(isDisplayed()));
    }

    public void testAddingItselfAsContactDoesNotWork() {
        enterAddContactsScreen();
        openAddContactDialog();

        String myEmail = TestUtils.VALUE_TEST_USER_ACCOUNT;
        onView(withHint(R.string.contact_email_address)).perform(typeText(myEmail));
        onView(withText(R.string.ok)).perform(click());

        pressBack();
        onView(allOf(withText(myEmail), withId(R.id.contact_email))).check(doesNotExist());
    }


    public void testAddingCustomContactSendsAllowRequest() throws JSONException, TimeoutException, InterruptedException{
        String contactEmail = "family.member@example.com";
        RequestsHandle requests = getMockDispatcher().setAllowPostResponse(new MockResponse().setResponseCode(200));

        enterAddContactsScreen();
        openAddContactDialog();
        onView(withHint(R.string.contact_email_address)).perform(typeText(contactEmail));
        onView(withText(R.string.ok)).perform(click());

        requests.waitUntilAnyRequests();
        RecordedRequest request = requests.getRequests().get(0);
        String expectedPath = "/user/" + TestUtils.VALUE_TEST_USER_ID + "/contacts/allow";
        assertEquals(expectedPath, request.getPath());
    }

    public void testAddingContactFromListSendsAllowRequest() throws JSONException, TimeoutException, InterruptedException{
        String contactEmail = "family.member@example.com";
        RequestsHandle requests = getMockDispatcher().setAllowPostResponse(new MockResponse().setResponseCode(200));

        enterAddContactsScreen();
        addContactFromContactListScreen(contactEmail, contactEmail);

        requests.waitUntilAnyRequests();
        RecordedRequest request = requests.getRequests().get(0);
        String expectedPath = "/user/" + TestUtils.VALUE_TEST_USER_ID + "/contacts/allow";
        assertEquals(expectedPath, request.getPath());
    }
}
