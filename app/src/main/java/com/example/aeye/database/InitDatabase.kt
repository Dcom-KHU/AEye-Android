package com.example.aeye.database

fun initDatabase() : List<ObjectInfo> {
    var list = emptyList<ObjectInfo>()

    list.plus(ObjectInfo("soda", "칠성사이다", ""))
    list.plus(ObjectInfo("pepsi", "펩시콜라", ""))
    list.plus(ObjectInfo("water", "생수", ""))
    list.plus(ObjectInfo("coffee", "레쓰비", ""))
    list.plus(ObjectInfo("zerosoda", "칠성사이다 제로", ""))
    list.plus(ObjectInfo("zeropepsi", "펩시콜라 제로슈거", ""))
    list.plus(ObjectInfo("hot6", "핫식스", ""))
    list.plus(ObjectInfo("milkis", "밀키스", ""))
    list.plus(ObjectInfo("pocari", "포카리 스웨트", ""))
    list.plus(ObjectInfo("cocacola", "코카콜라", ""))
    list.plus(ObjectInfo("zerococacola", "코카콜라 제로", ""))

    list.plus(ObjectInfo("tylenol", "타이레놀", ""))
    list.plus(ObjectInfo("fucidin", "후시딘", ""))
    list.plus(ObjectInfo("Brufen", "부루펜 시럽", ""))
    list.plus(ObjectInfo("easyend", "이지엔 식스 프로", ""))
    list.plus(ObjectInfo("Geworin", "게보린", ""))
    list.plus(ObjectInfo("Geworinsoft", "게보린 소프트", ""))
    list.plus(ObjectInfo("Gas", "가스활명수", ""))

    return list
}