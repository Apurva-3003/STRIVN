# STRIVN

[![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=flat&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Android](https://img.shields.io/badge/Android-3DDC84?style=flat&logo=android&logoColor=white)](https://developer.android.com/)
[![FastAPI](https://img.shields.io/badge/FastAPI-009688?style=flat&logo=fastapi&logoColor=white)](https://fastapi.tiangolo.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=flat&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Railway](https://img.shields.io/badge/Railway-0B0D0E?style=flat&logo=railway&logoColor=white)](https://railway.app/)

**STRIVN** is a full-stack Android fitness app for runners. Users log runs and daily check-ins; the app tracks **fitness**, **fatigue**, **sleep score**, and **daily training capacity** over time using a **server-side training model** backed by PostgreSQL.

---

## Tech stack

| Layer | Technologies |
|--------|----------------|
| **Android** | Kotlin, Jetpack Compose, Retrofit, OkHttp, ViewModel, StateFlow, EncryptedSharedPreferences (JWT) |
| **Backend** | Python, FastAPI, SQLAlchemy, PostgreSQL, JWT (HS256), bcrypt |
| **Deployment** | Railway (API + database) |

---

## Features

- **JWT authentication** — user registration and login with **encrypted** token persistence
- **Onboarding** — multi-step flow for goals, weekly mileage, experience, and related profile data
- **Run logging** — distance, duration, and RPE sent to the API; metrics updated from server response
- **Daily check-ins** — sleep, soreness, energy, and stress; server recomputes metrics
- **Server-side training model** — fitness, fatigue, capacity, and sleep score after each run and check-in
- **Home** — animated metric wheels reflecting current training state
- **Simulation** — preview impact of a recommended run, custom run, or rest day (aligned with backend formulas)
- **Progress** — weekly distance, consistency, longest run, and fitness/fatigue trends
- **Past runs** — history synced from the backend
- **Session handling** — automatic logout on **401 Unauthorized** via a global OkHttp interceptor

---

## Architecture

- The **Android** app talks to the **FastAPI** service over **HTTPS** using **Retrofit**.
- **Metric updates** are computed **on the server** and returned to the client; the app updates local state and UI from those responses.
- **PostgreSQL** on **Railway** stores users, runs, check-ins, and metrics history.
- The **JWT** is stored in **EncryptedSharedPreferences** so sessions survive app restarts.
- A **repository** layer separates network and persistence concerns from Compose screens and ViewModels.

High-level layout:

```
STRIVN/
├── app/          # Android app (Kotlin + Jetpack Compose)
└── backend/      # REST API (FastAPI + PostgreSQL)
```

---

## Running locally — Backend

From the `backend` directory:

```powershell
cd backend
python -m venv venv
.\venv\Scripts\Activate.ps1
pip install -r requirements.txt
$env:SECRET_KEY="yoursecretkey"
uvicorn app.main:app --reload --host 0.0.0.0
```

Set `SECRET_KEY` to a strong random string. Configure your database URL (e.g. via environment variables expected by your FastAPI app) so SQLAlchemy can connect to PostgreSQL.

---

## Running locally — Android

1. Open the project in **Android Studio**.
2. Set **`BASE_URL`** in `app/src/main/java/com/example/strivn/network/RetrofitClient.kt` to your backend (e.g. `http://<your-LAN-IP>:8000/` for a device or emulator reaching your machine).
3. Run the **app** module on an emulator or physical device.

---

## Deployment (Railway)

The FastAPI service and PostgreSQL database are intended to run on **Railway**. Configure Railway environment variables (including `SECRET_KEY` and database credentials) to match what the FastAPI app expects.

---

## Screenshots

Screenshots coming soon

---

## Demo

Demo video coming soon

---

## License

Specify your license here (e.g. MIT, Apache-2.0, or proprietary). This repository does not ship a default license file unless you add one.
