import pymysql

conn = pymysql.connect(
    host='209.146.116.109',
    user='admin',
    password='Qwer1234',
    database='litemall',
    charset='utf8mb4'
)

cursor = conn.cursor()

cursor.execute("SELECT key_name, key_value FROM litemall_system WHERE key_name LIKE '%point%' OR key_name LIKE '%积分%'")
results = cursor.fetchall()

print("=== 积分相关配置 ===")
for row in results:
    print(f"{row[0]}: {row[1]}")

cursor.close()
conn.close()
