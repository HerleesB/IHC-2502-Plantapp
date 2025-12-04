"""
CU-05: Modelos de datos para Comparador Visual de Plantas
Permite comparar fotos de antes/después del tratamiento
"""
from sqlalchemy import Column, Integer, String, Float, DateTime, ForeignKey, Text
from datetime import datetime
from app.models.database import Base


class PlantComparison(Base):
    """Modelo para almacenar comparaciones de fotos de plantas"""
    __tablename__ = "plant_comparisons"
    
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    diagnosis_id = Column(Integer, ForeignKey("diagnoses.id"), nullable=True)
    
    # Fotos
    before_image_path = Column(String(500), nullable=False)
    after_image_path = Column(String(500), nullable=False)
    
    # Análisis de cambios
    health_improvement = Column(Float, default=0.0)  # -100 a +100
    visual_changes = Column(Text, nullable=True)  # JSON con cambios detectados
    
    # Metadata
    days_between = Column(Integer, default=0)  # Días entre fotos
    treatment_applied = Column(String(500), nullable=True)  # Tratamiento aplicado
    notes = Column(Text, nullable=True)  # Notas del usuario
    
    # Timestamps
    before_date = Column(DateTime, default=datetime.utcnow)
    after_date = Column(DateTime, default=datetime.utcnow)
    created_at = Column(DateTime, default=datetime.utcnow)
    
    def __repr__(self):
        return f"<PlantComparison(id={self.id}, improvement={self.health_improvement}%)>"


class ComparisonMetric(Base):
    """Métricas específicas de la comparación"""
    __tablename__ = "comparison_metrics"
    
    id = Column(Integer, primary_key=True, index=True)
    comparison_id = Column(Integer, ForeignKey("plant_comparisons.id"), nullable=False)
    
    # Métricas de salud
    color_health_before = Column(Float, default=0.0)  # 0-100
    color_health_after = Column(Float, default=0.0)
    
    leaf_count_before = Column(Integer, default=0)
    leaf_count_after = Column(Integer, default=0)
    
    affected_area_before = Column(Float, default=0.0)  # Porcentaje
    affected_area_after = Column(Float, default=0.0)
    
    # Análisis visual
    disease_progression = Column(String(50), default="stable")  # improved/stable/worsened
    new_symptoms = Column(Text, nullable=True)  # Síntomas nuevos detectados
    
    created_at = Column(DateTime, default=datetime.utcnow)
    
    def __repr__(self):
        return f"<ComparisonMetric(comparison_id={self.comparison_id}, progression={self.disease_progression})>"
