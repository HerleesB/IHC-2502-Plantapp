"""
Script para generar diagrama de la base de datos
"""
import sqlite3

conn = sqlite3.connect('jardin.db')
cursor = conn.cursor()

print("=" * 80)
print("  📊 BASE DE DATOS - JARDÍN INTELIGENTE")
print("=" * 80)

# Obtener todas las tablas
cursor.execute("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name")
tables = cursor.fetchall()

print(f"\n📋 Total de tablas: {len(tables)}\n")

for table in tables:
    table_name = table[0]
    
    print("=" * 80)
    print(f"📁 TABLA: {table_name.upper()}")
    print("=" * 80)
    
    # Obtener información de columnas
    cursor.execute(f"PRAGMA table_info({table_name})")
    columns = cursor.fetchall()
    
    print("\n📝 Columnas:")
    for col in columns:
        col_id, name, col_type, not_null, default_val, pk = col
        
        # Indicadores
        indicators = []
        if pk:
            indicators.append("🔑 PK")
        if not_null:
            indicators.append("⚠️ NOT NULL")
        if default_val:
            indicators.append(f"📌 DEFAULT: {default_val}")
        
        indicator_str = " ".join(indicators) if indicators else ""
        print(f"  • {name:25s} {col_type:15s} {indicator_str}")
    
    # Obtener Foreign Keys
    cursor.execute(f"PRAGMA foreign_key_list({table_name})")
    fks = cursor.fetchall()
    
    if fks:
        print("\n🔗 Foreign Keys:")
        for fk in fks:
            fk_id, seq, ref_table, from_col, to_col, on_update, on_delete, match = fk
            print(f"  • {from_col} → {ref_table}.{to_col}")
            if on_delete != 'NO ACTION':
                print(f"    ON DELETE {on_delete}")
            if on_update != 'NO ACTION':
                print(f"    ON UPDATE {on_update}")
    
    # Contar registros
    try:
        cursor.execute(f"SELECT COUNT(*) FROM {table_name}")
        count = cursor.fetchone()[0]
        print(f"\n📊 Registros: {count}")
    except:
        pass
    
    print()

conn.close()

print("=" * 80)
print("✅ ANÁLISIS COMPLETADO")
print("=" * 80)
