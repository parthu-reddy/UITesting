# Templates

Contents:
1. Preflight question message
2. Session log
3. Stop report (backend issue or blocker)
4. Dev-override notice
5. Final summary

Keep reports short and specific. The user should be able to make a decision from the report alone, without re-reading the whole session.

---

## 1. Preflight question message

Send once, and only include items you could not discover yourself. State what you already found so the user can correct you.

```
Before I open the browser, I found: <framework, base URL, source location, ...>.
I still need:
1. <e.g. Which role and which flow should I cover?>
2. <e.g. How do I log in as that role (credentials source / OTP method)?>
3. <e.g. Where are the dummy images?>
4. <e.g. Where are the publish-one-service and deploy-one-service workflows?>
The base URL looks like <dev/staging>. Please confirm it is not production.
```

---

## 2. Session log

Keep a running log as you go (in the conversation, or in a scratch note that is not committed). It lets you resume accurately after a pause and makes the final summary easy.

```
Session: <role> / <flow> / <environment>
Test file: <path>

Step 1: <page name>
  URL: <route>
  Code: <component file(s)>
  Filled: <field -> value summary>
  Result: <what happened on submit; route/toast>
  Console/network: <clean | issue summary>
  Test step written: yes/no

Step 2: ...

Open items:
- <question or blocker>
Code/config changes so far:
- <file: what and why, commit, service, published/deployed?>
```

---

## 3. Stop report (backend issue or blocker)

```
STOPPED at: <flow> > <page/step>. The browser is still open on this page.

What I was doing:
<one or two sentences: the action and the data submitted>

What happened:
<observed behavior; exact UI message>

Evidence:
- Screenshot: <attached>
- URL: <current URL>
- Console: <error message(s), source file:line if any>
- Failed request: <METHOD URL -> status>
  Response: <body, redacted>
  Payload (relevant fields): <redacted>

Classification: <Backend issue | Third-party validation | Unknown> because <reason>.

Likely cause:
<hypothesis with file references, e.g. "<service>/<path>:<line> rejects X when ...">

What I need from you:
<the specific decision, e.g. "Proceed with a dev-profile-only workaround for <check>, or wait for a backend fix?">

I will not continue, reload, or close the browser until you reply.
```

---

## 4. Dev-override notice

Send briefly before making a dev-profile override (not a stop-and-wait unless something is risky or unclear).

```
<Integration> validation is blocking <step> in dev.
Why it exists: <reason>.
Proposed override: <existing flag | sandbox values | profile-gated stub | profile-gated fixed value>,
active only when profile = <dev/test>, inert in production because <reason>.
Service affected: <one service>. I will commit, push, publish and deploy only that service,
then retry this step in the open browser.
```

---

## 5. Final summary

```
E2E test summary: <role> / <flow>

Result
- Test file(s): <paths>
- Passed from a clean start: yes/no (runs: N)
- Terminal state reached: <page/state>

Coverage
- Pages/forms covered: <list in order>
- Not covered, and why: <list>
- Cross-role dependencies: <list or none>

Data and assets
- Dummy data approach: <unique per run, prefix, reserved domains>
- Fixture files added: <paths> (please review before committing)

Changes made along the way
- Test-id / UI fixes: <file: change>
- Dev-profile overrides: <what, where, which service, published/deployed>
- Preconditions the tests now rely on: <list or none>

Open issues (not fixed)
- <backend issue: short description, screenshot reference, status: waiting on you>

How to run
- <command>, required env vars: <names only>

Candidate follow-ups
- <validation messages observed that could become negative tests, other roles' flows, etc.>
```
