package com.fooddelivery.e2e.pages.restaurant;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** ApplicationOutletForm inside the restaurant's current draft. */
public class OutletRegistrationPage {
    private final Page page;
    public OutletRegistrationPage(Page page) {this.page=page;}
    public boolean isRegistrationVisible() {assertThat(page.getByRole(AriaRole.FORM,new Page.GetByRoleOptions().setName("Application outlet"))).isVisible();return true;}
    public void fillOutletName(String name) {page.getByLabel("Outlet name",new Page.GetByLabelOptions().setExact(true)).fill(name);}
    public void fillCoordinates(double lat,double lng) {page.getByLabel("Latitude",new Page.GetByLabelOptions().setExact(true)).fill(String.valueOf(lat));page.getByLabel("Longitude",new Page.GetByLabelOptions().setExact(true)).fill(String.valueOf(lng));}
    public void fillFssai(String fssai) {page.getByLabel("FSSAI licence number",new Page.GetByLabelOptions().setExact(true)).fill(fssai);}
    public void submit() {page.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName("Save outlet and continue").setExact(true)).click();assertThat(page.getByLabel("GST registration file",new Page.GetByLabelOptions().setExact(true))).isAttached();}
}
