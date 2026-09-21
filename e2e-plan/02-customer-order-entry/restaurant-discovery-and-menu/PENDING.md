# Pending work and validation notes

Both MenuCartUiTest menu cases passed: menu names/prices and absence of restaurant editing controls; existing out-of-stock items expose no ADD or quantity controls. These independent cases continued after the cart failure.

Broader discovery/search/category coverage is pending. UI checks do not establish server-side authorization. Background browser resource errors remain unattributed; do not treat these passes as validation of those resources.

Evidence: UITesting/target/surefire-reports/TEST-com.fooddelivery.e2e.tests.smoke.MenuCartUiTest.xml (two menu passes, one cart failure in the latest full class run). No inventory edits.
