package com.example.wildcard.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wildcard.data.FirebaseRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * ユーザー登録画面のロジックを担当するViewModel
 */
class HomeViewModel : ViewModel() {

    private val repository = FirebaseRepository()

    // 画面遷移などの一度きりのイベントをUIに通知するためのFlow
    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    /**
     * ルームへの参加処理を開始する
     * @param username ユーザー名
     * @param roomCode 合言葉
     */
    fun joinRoom(username: String, roomCode: String) {
        viewModelScope.launch {
            val result = repository.joinOrCreateRoom(username, roomCode)
            if (result.isSuccess) {
                // 成功したらダッシュボードへの遷移イベントを通知
                _navigationEvent.emit(NavigationEvent.NavigateToDashboard)
            } else {
                // 失敗したらエラーイベントを通知
                val errorMessage = result.exceptionOrNull()?.message ?: "不明なエラーが発生しました"
                _navigationEvent.emit(NavigationEvent.ShowError(errorMessage))
            }
        }
    }

    // ナビゲーションイベントを定義する sealed class
    sealed class NavigationEvent {
        object NavigateToDashboard : NavigationEvent()
        data class ShowError(val message: String) : NavigationEvent()
    }
}