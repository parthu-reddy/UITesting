package com.fooddelivery.e2e.pages.common;

/** A destination selected after the person's one login. */
public enum Portal {
    CUSTOMER("Customer", "/customer"),
    RESTAURANT("Restaurant", "/restaurant"),
    DELIVERY("Delivery rider", "/delivery"),
    BUSINESS("Business", "/business"),
    ADMIN("Administrator", "/admin"),
    /** Ads Manager (A4): a BUSINESS portal of its own. */
    ADS("Ads Manager", "/ads");

    public final String label;
    public final String path;
    Portal(String label, String path) { this.label = label; this.path = path; }
}
