package com.fooddelivery.e2e.pages.business;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.util.regex.Pattern;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Drives the real organisation and invitation controls; no backend or stored browser state. */
public final class BusinessHubPage {
    private final Page page;
    public BusinessHubPage(Page page) { this.page = page; }

    /** The UI shows "+91 98765 43210"; tests keep the plain ten-digit number. */
    public static String displayPhone(String phone) {
        return phone.matches("\\d{10}") ? "+91 " + phone.substring(0, 5) + " " + phone.substring(5) : phone;
    }

    /** The UI shows roles as words ("Manager"); tests keep the wire code ("MANAGER"). */
    public static String roleLabel(String role) {
        return role.charAt(0) + role.substring(1).toLowerCase(java.util.Locale.ROOT);
    }
    public void create(String name) {
        Locator form = page.getByRole(AriaRole.FORM, new Page.GetByRoleOptions().setName("Create organisation").setExact(true));
        form.getByLabel("Organisation name").fill(name);
        form.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Create organisation").setExact(true)).click();
        page.waitForURL(Pattern.compile(".*/business/[a-f0-9-]{36}(?:/restaurant-application)?(?:[?#].*)?$"));
    }
    public Locator organisation(String name) { return page.getByTestId("organisation-card").filter(new Locator.FilterOptions().setHasText(name)); }
    public void open(String name) { organisation(name).getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Open organisation").setExact(true)).click(); }
    public String openFirstApproved() {
        Locator card = page.getByTestId("organisation-card").filter(new Locator.FilterOptions().setHasText("Approved")).first();
        card.waitFor(); String name = card.getByRole(AriaRole.HEADING).innerText();
        card.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Open organisation").setExact(true)).click(); return name;
    }
    /**
     * After openPortal(BUSINESS): a person with exactly one organisation lands on its page, anyone else on
     * the hub list. Opens the first approved organisation either way and returns its name.
     */
    public String openApprovedOrganisationAfterLaunch() {
        Locator list = page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Business hub").setExact(true));
        Locator orgPage = page.getByTestId("organisation-role");
        list.or(orgPage).first().waitFor();
        if (list.isVisible()) return openFirstApproved();
        assertThat(page.getByText("Approved", new Page.GetByTextOptions().setExact(true))).isVisible();
        return page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setLevel(1)).first().innerText().trim();
    }
    public void allOrganisations() { button("All organisations").click(); assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Business hub").setExact(true))).isVisible(); }
    public void members() { page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("Members").setExact(true)).click(); }
    public void invite(String phone, String role) {
        Locator form = page.getByRole(AriaRole.FORM, new Page.GetByRoleOptions().setName("Invite a colleague").setExact(true));
        form.getByLabel("Phone number").fill(phone); chooseRole("Invitation role", roleLabel(role)); button("Invite colleague").click();
        assertThat(page.getByTestId("organisation-invitation").filter(new Locator.FilterOptions().setHasText(displayPhone(phone)))).containsText("Pending");
    }
    public void accept(String organisationName) {
        Locator invite = page.getByTestId("my-invitation").filter(new Locator.FilterOptions().setHasText("Invitation to " + organisationName));
        invite.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Accept invitation").setExact(true)).click();
        assertThat(invite).hasCount(0); assertThat(organisation(organisationName)).isVisible();
    }
    public Locator member(String phone) { return page.getByTestId("organisation-member").filter(new Locator.FilterOptions().setHasText(displayPhone(phone))); }
    public void changeRole(String phone, String role) {
        String label = roleLabel(role);
        member(phone).getByRole(AriaRole.COMBOBOX).click(); option(label).click();
        Locator dialog = page.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName("Change role?").setExact(true));
        dialog.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Change role").setExact(true)).click();
        assertThat(member(phone).getByRole(AriaRole.COMBOBOX)).containsText(label);
        assertThat(member(phone)).containsText(label);
    }
    public void remove(String phone) {
        member(phone).getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Remove member").setExact(true)).click();
        Locator dialog = page.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName("Remove this member?").setExact(true));
        dialog.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Remove member").setExact(true)).click();
        assertThat(member(phone)).hasCount(0);
    }
    public void application() { page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("^(Start a restaurant|View restaurant application)$"))).click(); assertThat(page.getByTestId("restaurant-application")).isVisible(); }
    public void chooseRole(String label, String value) { page.getByRole(AriaRole.COMBOBOX, new Page.GetByRoleOptions().setName(label).setExact(true)).click(); option(value).click(); }
    private Locator option(String name) { return page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName(name).setExact(true)); }
    private Locator button(String name) { return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(name).setExact(true)); }
}
