"""Base de datos SQLAlchemy"""
from sqlalchemy import create_engine, Column, Integer, String, Float, Boolean, DateTime, Text, ForeignKey
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker, relationship
from datetime import datetime
import os

SQLALCHEMY_DATABASE_URL = "sqlite:///./jardin.db"
engine = create_engine(SQLALCHEMY_DATABASE_URL, connect_args={"check_same_thread": False})
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()

# ========== MODELOS ORM ==========

class UserDB(Base):
    __tablename__ = "users"
    id = Column(Integer, primary_key=True, index=True)
    email = Column(String, unique=True, index=True)
    username = Column(String, unique=True, index=True)
    full_name = Column(String, nullable=True)
    hashed_password = Column(String)
    level = Column(Integer, default=1)
    xp = Column(Integer, default=0)
    points = Column(Integer, default=0)
    streak_days = Column(Integer, default=0)
    last_activity = Column(DateTime, nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow)
    plants = relationship("PlantDB", back_populates="owner")
    diagnoses = relationship("DiagnosisDB", back_populates="user")


class PlantDB(Base):
    __tablename__ = "plants"
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"))
    name = Column(String, index=True)
    species = Column(String, nullable=True)
    description = Column(Text, nullable=True)
    image_url = Column(String, nullable=True)
    location = Column(String, nullable=True)
    status = Column(String, default="healthy")
    health_score = Column(Integer, default=100)
    last_watered = Column(DateTime, nullable=True)
    last_fertilized = Column(DateTime, nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow)
    owner = relationship("UserDB", back_populates="plants")
    diagnoses = relationship("DiagnosisDB", back_populates="plant")


class DiagnosisDB(Base):
    __tablename__ = "diagnoses"
    id = Column(Integer, primary_key=True, index=True)
    plant_id = Column(Integer, ForeignKey("plants.id"), nullable=True)
    user_id = Column(Integer, ForeignKey("users.id"))
    image_url = Column(String)
    diagnosis_text = Column(Text)
    confidence = Column(Float)
    disease_name = Column(String, nullable=True)
    severity = Column(String)
    recommendations = Column(Text)  # JSON string
    created_at = Column(DateTime, default=datetime.utcnow)
    is_shared = Column(Boolean, default=False)
    plant = relationship("PlantDB", back_populates="diagnoses")
    user = relationship("UserDB", back_populates="diagnoses")


class CommunityPostDB(Base):
    __tablename__ = "community_posts"
    id = Column(Integer, primary_key=True, index=True)
    diagnosis_id = Column(Integer, ForeignKey("diagnoses.id"))
    user_id = Column(Integer, ForeignKey("users.id"))
    is_anonymous = Column(Boolean, default=False)
    likes = Column(Integer, default=0)
    comments_count = Column(Integer, default=0)
    status = Column(String, default="pending")
    # Campos adicionales para CU-18 (publicación con imagen directa)
    description = Column(Text, nullable=True)
    plant_name = Column(String, nullable=True)
    symptoms = Column(String, nullable=True)
    image_url = Column(String, nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow)


class CommentDB(Base):
    __tablename__ = "comments"
    id = Column(Integer, primary_key=True, index=True)
    post_id = Column(Integer, ForeignKey("community_posts.id"))
    user_id = Column(Integer, ForeignKey("users.id"))
    content = Column(Text)
    is_solution = Column(Boolean, default=False)
    likes = Column(Integer, default=0)
    created_at = Column(DateTime, default=datetime.utcnow)


class PostLikeDB(Base):
    """Tabla para rastrear likes únicos por usuario"""
    __tablename__ = "post_likes"
    id = Column(Integer, primary_key=True, index=True)
    post_id = Column(Integer, ForeignKey("community_posts.id"))
    user_id = Column(Integer, ForeignKey("users.id"))
    created_at = Column(DateTime, default=datetime.utcnow)


class AchievementDB(Base):
    __tablename__ = "achievements"
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"))
    name = Column(String)
    description = Column(Text)
    icon = Column(String)
    points = Column(Integer)
    unlocked = Column(Boolean, default=False)
    unlocked_at = Column(DateTime, nullable=True)


class DiagnosisFeedbackDB(Base):
    """CU-12: Feedback de diagnóstico para Active Learning"""
    __tablename__ = "diagnosis_feedback"
    id = Column(Integer, primary_key=True, index=True)
    diagnosis_id = Column(Integer, ForeignKey("diagnoses.id"))
    user_id = Column(Integer, ForeignKey("users.id"))
    is_correct = Column(Boolean)
    correct_diagnosis = Column(String, nullable=True)
    feedback_text = Column(Text, nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow)


class ReminderDB(Base):
    """CU-06: Recordatorios de cuidado"""
    __tablename__ = "reminders"
    id = Column(Integer, primary_key=True, index=True)
    plant_id = Column(Integer, ForeignKey("plants.id"))
    user_id = Column(Integer, ForeignKey("users.id"))
    reminder_type = Column(String)  # "water", "fertilize", "diagnose", "check"
    message = Column(String)
    scheduled_time = Column(DateTime)
    completed = Column(Boolean, default=False)
    completed_at = Column(DateTime, nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow)


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


def init_db():
    """Inicializar base de datos y crear tablas"""
    Base.metadata.create_all(bind=engine)


def reset_db():
    """Reiniciar base de datos (eliminar y recrear tablas)"""
    Base.metadata.drop_all(bind=engine)
    Base.metadata.create_all(bind=engine)
