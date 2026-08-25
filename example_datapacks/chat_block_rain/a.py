import json
import urllib.request

LANG_URL = "https://raw.githubusercontent.com/misode/mcmeta/26.2-assets/assets/minecraft/lang/ko_kr.json"
BLOCK_URL = "https://raw.githubusercontent.com/misode/mcmeta/26.2-registries/block/data.json"

# 요청 헤더에 User-Agent 추가 (차단 방지)
headers = {"User-Agent": "Mozilla/5.0"}

def fetch_json(url):
    req = urllib.request.Request(url, headers=headers)
    with urllib.request.urlopen(req) as response:
        return json.loads(response.read().decode("utf-8"))

# 딕셔너리와 리스트로 가져오기
lang_dict = fetch_json(LANG_URL)
block_list = fetch_json(BLOCK_URL)

PREFIX = "block.minecraft."

final_dict = {}
missing_blocks = []

for block in block_list:
    # 1. 번역 유무와 무관하게 영문 ID는 항상 등록 (ex: 'white_wall_banner': 'white_wall_banner')
    final_dict[block] = block
    
    full_key = f"{PREFIX}{block}"
    
    # 2. 번역명이 있으면 한글명(공백제거)도 함께 등록
    if full_key in lang_dict:
        trans_name = lang_dict[full_key].replace(" ", "")
        final_dict[trans_name] = block
    else:
        print(f"[번역 없음] {block}")
        missing_blocks.append(block)

print(f"\n총 {len(missing_blocks)}개의 블록 번역이 누락되었으나, 영문 키로 등록 완료했습니다.")

# JSON 파일로 저장
with open("final_dict.json", "w", encoding="utf-8") as f:
    json.dump(final_dict, f, ensure_ascii=False, separators=(',', ':'))