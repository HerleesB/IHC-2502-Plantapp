"""
Script para encontrar endpoints en plants.py
"""
with open('app/routes/plants.py', 'r', encoding='utf-8') as f:
    lines = f.readlines()

print("=" * 70)
print("ENDPOINTS ENCONTRADOS EN plants.py:")
print("=" * 70)

for i, line in enumerate(lines, 1):
    if '@router.' in line and ('post' in line.lower() or 'get' in line.lower() or 'delete' in line.lower() or 'put' in line.lower()):
        print(f"\nLínea {i}: {line.strip()}")
        # Mostrar las siguientes 2 líneas también
        if i < len(lines):
            print(f"        {lines[i].strip()}")
        if i+1 < len(lines):
            print(f"        {lines[i+1].strip()}")

print("\n" + "=" * 70)
