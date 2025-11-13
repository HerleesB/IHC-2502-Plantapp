"""Esquemas Pydantic para validación de datos"""
from pydantic import BaseModel, Field, EmailStr
from typing import Optional, List
from datetime import datetime
from enum import Enum

# ========== USUARIOS ==========
class UserBase(BaseModel):
    email: EmailStr
    username: str
    full_name: Optional[str] = None

class UserCreate(UserBase):
    password: str

class User(UserBase):
    id: int
    level: int = 1
    xp: int = 0
    points: int = 0
    streak_days: int = 0
    created_at: datetime
    
    class Config:
        from_attributes = True

# ========== PLANTAS ==========
class PlantStatus(str, Enum):
    healthy = "healthy"
    warning = "warning"
    critical = "critical"

class PlantBase(BaseModel):
    name: str
    species: Optional[str] = None
    description: Optional[str] = None
    image_url: Optional[str] = None

class PlantCreate(PlantBase):
    user_id: int

class Plant(PlantBase):
    id: int
    user_id: int
    status: PlantStatus = PlantStatus.healthy
    health_score: int = 100
    last_watered: Optional[datetime] = None
    last_fertilized: Optional[datetime] = None
    created_at: datetime
    
    class Config:
        from_attributes = True

# ========== DIAGNÓSTICOS ==========
class DiagnosisCreate(BaseModel):
    plant_id: int
    image_base64: str
    symptoms: Optional[str] = None

class Diagnosis(BaseModel):
    id: int
    plant_id: int
    user_id: int
    image_url: str
    diagnosis_text: str
    confidence: float
    disease_name: Optional[str] = None
    severity: str
    recommendations: List[str]
    created_at: datetime
    is_shared: bool = False
    
    class Config:
        from_attributes = True

# ========== PLAN SEMANAL ==========
class WeeklyTask(BaseModel):
    id: int
    plant_id: int
    task_type: str  # riego, fertilizar, podar, etc
    description: str
    scheduled_date: datetime
    completed: bool = False
    completed_at: Optional[datetime] = None

class WeeklyPlan(BaseModel):
    plant_id: int
    tasks: List[WeeklyTask]

# ========== RECORDATORIOS ==========
class ReminderCreate(BaseModel):
    plant_id: int
    reminder_type: str
    scheduled_time: datetime
    message: str

class Reminder(ReminderCreate):
    id: int
    user_id: int
    sent: bool = False
    created_at: datetime

# ========== GAMIFICACIÓN ==========
class Achievement(BaseModel):
    id: int
    name: str
    description: str
    icon: str
    points: int
    unlocked: bool = False
    unlocked_at: Optional[datetime] = None

class Mission(BaseModel):
    id: int
    title: str
    description: str
    reward_xp: int
    reward_points: int
    progress: int
    target: int
    completed: bool = False
    expires_at: datetime

# ========== COMUNIDAD ==========
class CommunityPostCreate(BaseModel):
    diagnosis_id: int
    is_anonymous: bool = False
    tags: Optional[List[str]] = None

class CommunityPost(BaseModel):
    id: int
    diagnosis_id: int
    user_id: int
    author_name: str
    is_anonymous: bool
    likes: int = 0
    comments_count: int = 0
    status: str  # pending, resolved
    created_at: datetime
    
    class Config:
        from_attributes = True

class CommentCreate(BaseModel):
    post_id: int
    content: str
    is_solution: bool = False

class Comment(CommentCreate):
    id: int
    user_id: int
    author_name: str
    likes: int = 0
    created_at: datetime

# ========== RESPUESTAS API ==========
class DiagnosisResponse(BaseModel):
    diagnosis_id: int
    diagnosis_text: str
    disease_name: Optional[str]
    confidence: float
    severity: str
    recommendations: List[str]
    weekly_plan: List[dict]
    audio_url: Optional[str] = None

class CaptureGuidance(BaseModel):
    step: str
    message: str
    success: bool
    guidance: str
    audio_url: Optional[str] = None

class ProgressStats(BaseModel):
    total_plants: int
    healthy_plants: int
    diagnoses_count: int
    streak_days: int
    level: int
    xp: int
    next_level_xp: int
