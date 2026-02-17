import pymysql

conn = pymysql.connect(
    host='209.146.116.109',
    user='admin',
    password='Qwer1234',
    database='litemall',
    charset='utf8mb4'
)

cursor = conn.cursor()

new_config = '[{"amount":"0","rate":"10"},{"amount":"10000","rate":"10"},{"amount":"20000","rate":"11"},{"amount":"30000","rate":"11.5"},{"amount":"50000","rate":"12"},{"amount":"100000","rate":"12.5"},{"amount":"200000","rate":"13"}]'

sql = "UPDATE litemall_system SET key_value = %s WHERE key_name = 'litemall_point_reward_tiers'"
cursor.execute(sql, (new_config,))
conn.commit()

print(f"更新了 {cursor.rowcount} 行")

cursor.execute("SELECT key_value FROM litemall_system WHERE key_name = 'litemall_point_reward_tiers'")
result = cursor.fetchone()
print(f"新配置: {result[0]}")

cursor.close()
conn.close()
