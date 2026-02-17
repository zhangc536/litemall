import pymysql

conn = pymysql.connect(
    host='209.146.116.109',
    user='admin',
    password='Qwer1234',
    database='litemall',
    charset='utf8mb4'
)

cursor = conn.cursor()

print("=== 数据库中的所有表 ===")
cursor.execute("SHOW TABLES")
tables = cursor.fetchall()
for t in tables:
    print(f"  {t[0]}")

print(f"\n总共 {len(tables)} 个表")

cursor.close()
conn.close()
