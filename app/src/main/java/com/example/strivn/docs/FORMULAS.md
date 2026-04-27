# Strivn Training Model Formulas

This document describes the formulas used to calculate fatigue, fitness, Daily Training Capacity (DTC), injury risk, and workout recommendations.

---

## 1. Training Load (`BanisterFitnessFatigueModel`)

**Formula:** `trainingLoad = duration × RPE`

- `duration`: Run duration in **minutes**
- `RPE`: Rate of Perceived Exertion (1–10)

---

## 2. Fitness (`BanisterFitnessFatigueModel`)

**Formula (discrete update):**
```
fitness_gain  = trainingLoad × 0.08   (slow adaptation)
fitness_decay = previous_fitness × 0.02   (slow detraining)
new_fitness   = clamp(previous_fitness + fitness_gain - fitness_decay, 0, 100)
```

- Fitness increases slowly with training load (gain coefficient 0.08).
- Fitness decays slowly over time when there is no stimulus (2% per update).
- Values are clamped to 0–100.

---

## 3. Fatigue (`BanisterFitnessFatigueModel`)

**Formula (discrete update):**
```
fatigue_gain     = trainingLoad × 0.40   (fast accumulation)
fatigue_recovery = recovery_modifier × 0.25 × 15.0   (reduced by sleep/recovery)
fatigue_decay    = previous_fatigue × 0.08   (baseline decay)
new_fatigue      = clamp(previous_fatigue + fatigue_gain - fatigue_recovery - fatigue_decay, 0, 100)
```

- Fatigue increases quickly with load (gain 0.40, faster than fitness).
- Fatigue is reduced by a **recovery modifier** (0–1), which is computed from:
  - Sleep hours (optimal 7–9 h)
  - Sleep quality (1–5 → 0–1)
  - Muscle soreness (1–5, inverted)
  - Energy level (1–5 → 0–1)
  - Stress level (1–5, inverted)
- Weights: sleep 30%, quality 25%, soreness 20%, energy 15%, stress 10%.

---

## 4. Daily Training Capacity (DTC) (`BanisterFitnessFatigueModel`)

**Formula:**
```
delta     = clamp(fitness - fatigue, -100, 100)
DTC       = round(((delta + 100) / 200) × 100)
DTC       = clamp(DTC, 0, 100)
```

- Maps the difference `fitness − fatigue` from [-100, 100] to [0, 100].
- Higher DTC = more capacity for training; lower = need more recovery.

---

## 5. Injury Risk (`BanisterFitnessFatigueModel`)

**Formula:**
```
injury_risk = clamp(
  fatigue_contribution + spike_penalty + load_penalty + injury_penalty,
  0, 100
)
```

- **Fatigue contribution:** `fatigue × 0.50` (up to 50).
- **Spike penalty:** load increase vs. previous session:
  - >50% increase: +20
  - >25% increase: +12
  - >10% increase: +5
- **Load penalty:** absolute training load:
  - >400: +15
  - >300: +10
  - >200: +5
- **Injury penalty:** +25 if `UserProfile.injuryStatus == true`.
- Result is clamped to 0–100.

---

## 6. Workout Recommendation (`ReadinessEngine`)

**Logic:** Choose run type based on capacity, fatigue, and sleep thresholds:

| Condition                                      | Recommendation   | Focus               | RPE | Distance | Duration |
|-----------------------------------------------|------------------|---------------------|-----|----------|----------|
| capacity ≥ 70, fatigue ≤ 55, sleep ≥ 65       | Tempo Run        | Aerobic Threshold   | 6   | 6.0 km   | 48 min   |
| capacity ≥ 55, fatigue ≤ 70                    | Steady Run       | Aerobic Base        | 5   | 5.0 km   | 40 min   |
| else                                           | Recovery Run     | Easy Aerobic        | 3   | 3.5 km   | 28 min   |

- **Injury risk** for the recommendation comes from `UserMetrics.injuryRisk` (Banister model output).
- Environment is always "Outdoor" in the current implementation.

---

## 7. Readiness State / Observation (`ReadinessEngine`)

**Logic:** Maps capacity, fatigue, and sleep to a readiness state and color:

| State                  | Condition                                  |
|------------------------|--------------------------------------------|
| Primed for Progress    | capacity ≥ 70, fatigue ≤ 45, sleep ≥ 70   |
| Stable & Building      | capacity ≥ 60, fatigue ≤ 60               |
| Under-Recovered        | fatigue ≥ 75, sleep ≤ 55                  |
| High Strain            | fatigue ≥ 70                               |
| Recovering             | capacity ≤ 45                             |
| Maintenance Mode       | else                                       |

**Color:**
- Green (StrivnAccent): capacity ≥ 65, fatigue ≤ 60, sleep ≥ 65
- Yellow/orange (StrivnWarning): capacity ≥ 50, sleep ≥ 55
- Red (StrivnError): else
