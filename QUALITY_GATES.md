# Quality Gates Setup Guide

## Overview

Quality Gates are **pass/fail checks** that determine if your code meets minimum quality standards. You can enforce them in **two ways**:

1. **CI Pipeline** - Fail the build if quality gates aren't met
2. **GitHub Branch Protection** - Block merges if CI checks fail

**Best Practice**: Use **both** for maximum protection! 🛡️

---

## 🔍 How It Works Now

### Current CI Configuration

Your CI pipeline now includes **quality gate checking**:

```yaml
-Dsonar.qualitygate.wait=true
```

**What this does:**
- ✅ Analyzes your code in SonarQube
- ✅ Waits for SonarQube to compute the quality gate status
- ✅ **FAILS the build** if quality gate is not passed
- ✅ Blocks the CI pipeline from continuing

**Key Changes Made:**
```yaml
# Before (lenient - always passes)
continue-on-error: true

# After (strict - fails build on quality gate failure)
continue-on-error: false
-Dsonar.qualitygate.wait=true
```

---

## 📊 Method 1: Quality Gate in CI Pipeline (Currently Active)

### How It Works:

1. **Developer pushes code** to GitHub
2. **CI pipeline runs** (GitHub Actions)
3. **Tests execute** with JaCoCo coverage
4. **SonarQube analysis** runs
5. **Quality gate evaluation** happens
6. ❌ **Build FAILS** if quality gate not met
7. ✅ **Build PASSES** if quality gate met

### Advantages:
- ✅ Immediate feedback in CI logs
- ✅ Prevents bad code from being merged
- ✅ Visible in GitHub PR checks
- ✅ Can be configured per branch

### Configuration:

**In SonarQube UI** (http://localhost:9000):
1. Go to **Quality Gates** → **Create** or use default "Sonar way"
2. Set conditions (e.g., Coverage > 80%, Bugs = 0)
3. Assign to your project (`tqsbackend`)

**Default Quality Gate Conditions:**
- No new bugs
- No new vulnerabilities
- No new security hotspots
- Coverage on new code ≥ 80%
- Duplicated lines on new code ≤ 3%

---

## 🔒 Method 2: GitHub Branch Protection Rules

### Setup Steps:

1. **Go to GitHub Repository** → **Settings** → **Branches**

2. **Add Branch Protection Rule** for `main`:
   ```
   Branch name pattern: main
   ```

3. **Check these options:**
   - ☑️ **Require a pull request before merging**
     - ☑️ Require approvals: 1 (or more)
   - ☑️ **Require status checks to pass before merging**
     - ☑️ Require branches to be up to date before merging
     - Search and add:
       - `build-and-test` (from CI pipeline)
       - `code-quality` (from quality workflow)
   - ☑️ **Require conversation resolution before merging**
   - ☑️ **Do not allow bypassing the above settings**

4. **Save changes**

### What This Does:
- 🚫 **Blocks direct pushes** to `main` branch
- 🚫 **Prevents merging** if CI checks fail
- 🚫 **Requires PR approval** before merge
- ✅ Forces all changes through pull requests
- ✅ Ensures quality gates pass before merge

---

## 🎯 Recommended Setup (Both Methods Combined)

### For Maximum Protection:

```
┌─────────────────────────────────────────────────┐
│  Developer creates Pull Request                 │
└─────────────────────┬───────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────┐
│  CI Pipeline Runs                               │
│  ├── Build & Test                               │
│  ├── SonarQube Analysis                         │
│  └── Quality Gate Check ✓                       │
└─────────────────────┬───────────────────────────┘
                      │
                      ▼
          ┌───────────┴───────────┐
          │  Quality Gate         │
          │  Status Check         │
          └───────────┬───────────┘
                      │
          ┌───────────┴───────────┐
          │                       │
          ▼                       ▼
    ✅ PASSED                ❌ FAILED
          │                       │
          │                       ▼
          │              ┌────────────────┐
          │              │ Build Fails    │
          │              │ PR Blocked     │
          │              │ Can't Merge    │
          │              └────────────────┘
          │
          ▼
┌─────────────────────────────────────────────────┐
│  GitHub Branch Protection                       │
│  ├── Check if CI passed ✓                       │
│  ├── Check if approved ✓                        │
│  └── Allow merge ✓                              │
└─────────────────────────────────────────────────┘
```

---

## 🛠️ Configuring Quality Gates in SonarQube

### 1. Access SonarQube
```bash
# Make sure SonarQube is running
./docker.sh dev  # or ./docker.sh prod

# Open browser
http://localhost:9000
```

### 2. Configure Quality Gate

**Administration** → **Quality Gates** → **Create** (or edit "Sonar way")

#### Recommended Conditions:

| Metric | Operator | Value | Description |
|--------|----------|-------|-------------|
| **Coverage** | is less than | 80% | Code coverage must be ≥ 80% |
| **Duplicated Lines (%)** | is greater than | 3% | Max 3% duplicate code |
| **Maintainability Rating** | is worse than | A | Must maintain A rating |
| **Reliability Rating** | is worse than | A | No bugs allowed |
| **Security Rating** | is worse than | A | No vulnerabilities |
| **Bugs** | is greater than | 0 | Zero tolerance for bugs |
| **Vulnerabilities** | is greater than | 0 | Zero vulnerabilities |
| **Code Smells** | is greater than | 10 | Max 10 code smells |

### 3. Assign to Project

1. Select your quality gate
2. Click **Projects** tab
3. Add `tqsbackend` project

---

## 🧪 Testing the Setup

### Test 1: Quality Gate Pass
```bash
# Write good quality code
# Push to feature branch
git checkout -b feature/test-quality-gate
git add .
git commit -m "Test: Good quality code"
git push origin feature/test-quality-gate

# Create PR in GitHub
# ✅ CI should pass
# ✅ Quality gate should pass
# ✅ Merge should be allowed
```

### Test 2: Quality Gate Fail
```bash
# Intentionally add code with issues:
# - Low test coverage
# - Code smells
# - Security vulnerabilities

git checkout -b feature/test-quality-gate-fail
# ... make changes with issues ...
git add .
git commit -m "Test: Poor quality code"
git push origin feature/test-quality-gate-fail

# Create PR in GitHub
# ❌ CI should fail
# ❌ Quality gate should fail
# ❌ Merge should be blocked
```

---

## 📋 Quality Gate Status in GitHub

### How to View:

1. **In Pull Request:**
   - Scroll to bottom of PR
   - See "Checks" section
   - Look for:
     - ✅ `build-and-test` - green check
     - ✅ `code-quality` - green check
   - Click "Details" to see logs

2. **In Actions Tab:**
   - Go to **Actions** tab
   - Click on workflow run
   - See detailed logs
   - SonarQube analysis results

3. **In SonarQube Dashboard:**
   - http://localhost:9000
   - View detailed analysis
   - See which conditions failed

---

## 🎛️ Flexibility Options

### Option A: Strict (Recommended for Production)
```yaml
-Dsonar.qualitygate.wait=true
continue-on-error: false
```
- ✅ Enforces quality standards
- ✅ Blocks bad code
- ❌ Can slow down development if gates are too strict

### Option B: Warning Mode (For Development)
```yaml
-Dsonar.qualitygate.wait=true
continue-on-error: true
```
- ✅ Shows quality gate status
- ⚠️ Doesn't block build
- ❌ Can allow bad code to merge

### Option C: Conditional Enforcement
```yaml
# Only enforce on main/develop branches
-Dsonar.qualitygate.wait=${{ github.ref == 'refs/heads/main' && 'true' || 'false' }}
```

---

## 🚦 Current Status of Your Setup

### ✅ What's Enabled:

- ✅ Quality gate checking in CI pipeline
- ✅ Build fails if quality gate not met
- ✅ SonarQube analysis on every push
- ✅ Code coverage tracking with JaCoCo
- ✅ Both `ci.yml` and `code-quality.yml` enforce quality gates

### ❌ What's Not Configured Yet:

- ❌ GitHub branch protection rules (you need to set this up manually)
- ❌ Custom quality gate conditions (using SonarQube defaults)
- ❌ Notification settings for failed quality gates

---

## 📝 Action Items for You

### Required:
1. ✅ **Set up GitHub Branch Protection** (see Method 2 above)
2. ✅ **Configure Quality Gates in SonarQube** (see section above)
3. ✅ **Test the setup** with a sample PR

### Optional:
4. ⚪ **Customize quality gate conditions** to your team's standards
5. ⚪ **Set up notifications** for quality gate failures
6. ⚪ **Add quality gate badge** to README

---

## 🎯 Summary

| Aspect | CI Pipeline | GitHub Protection |
|--------|-------------|-------------------|
| **Controls** | Build success/failure | Merge permission |
| **Enforces** | Quality standards | PR requirements |
| **Blocks** | CI pipeline | Merge button |
| **Configured by** | Workflow YAML | GitHub Settings |
| **Currently Active** | ✅ Yes | ❌ You need to enable |

**Recommendation:** 
- ✅ Keep CI quality gate checking (already configured)
- ✅ Add GitHub branch protection (you configure manually)
- ✅ This gives you **defense in depth**!

---

## 🔗 Quick Links

- [Configure Branch Protection](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-protected-branches/about-protected-branches)
- [SonarQube Quality Gates](https://docs.sonarqube.org/latest/user-guide/quality-gates/)
- [GitHub Actions Status Checks](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/collaborating-on-repositories-with-code-quality-features/about-status-checks)

---

**Your CI pipeline now enforces quality gates! 🎉**

**Next step:** Set up GitHub branch protection to complete the setup.
