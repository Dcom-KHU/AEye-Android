package com.example.aeye.database

import android.content.Context
import com.example.aeye.model.ObjectInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

val PRE_POPULATED_DATA = listOf(
    /* 음료 부분 */
    ObjectInfo("soda", "칠성사이다", ""),
    ObjectInfo("pepsi", "펩시콜라", ""),
    ObjectInfo("water", "생수", ""),
    ObjectInfo("coffee", "레쓰비", ""),
    ObjectInfo("zerosoda", "칠성사이다 제로", ""),
    ObjectInfo("zeropepsi", "펩시콜라 제로슈거", ""),
    ObjectInfo("hot6", "핫식스", ""),
    ObjectInfo("milkis", "밀키스", ""),
    ObjectInfo("pocari", "포카리 스웨트", ""),
    ObjectInfo("cocacola", "코카콜라", ""),
    ObjectInfo("zerococacola", "코카콜라 제로", ""),
    /* 의약품 부분 */
    ObjectInfo("tylenol", "타이레놀", ""),
    ObjectInfo("fucidin", "후시딘", ""),
    ObjectInfo("Brufen", "부루펜 시럽", ""),
    ObjectInfo("easyend", "이지엔 식스 프로", ""),
    ObjectInfo("Geworin", "게보린", ""),
    ObjectInfo("Geworinsoft", "게보린 소프트", ""),
    ObjectInfo("Gas", "가스활명수", ""),
    /* undefined */
    ObjectInfo("undefined", "물체 탐지 전", "")
)

fun initData(applicationContext: Context, scope: CoroutineScope) {
    scope.launch {
        ObjectInfoDatabase.getDataBase(applicationContext, scope).objectInfoDao()
            .insertPreliminaryData(
                PRE_POPULATED_DATA
            )
    }
}