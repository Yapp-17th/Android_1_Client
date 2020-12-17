package com.yapp.picon.presentation.di

import com.yapp.picon.presentation.collect.CollectViewModel
import com.yapp.picon.presentation.login.LoginViewModel
import com.yapp.picon.presentation.map.MapViewModel
import com.yapp.picon.presentation.nav.NavViewModel
import com.yapp.picon.presentation.nav.UserInfoViewModel
import com.yapp.picon.presentation.nav.manageFriend.ManageFriendViewModel
import com.yapp.picon.presentation.pingallery.PinGalleryViewModel
import com.yapp.picon.presentation.postedit.EditPostViewModel
import com.yapp.picon.presentation.post.PostViewModel
import com.yapp.picon.presentation.postdetail.PostDetailViewModel
import com.yapp.picon.presentation.profile.FriendProfileViewModel
import com.yapp.picon.presentation.profile.MyProfileViewModel
import com.yapp.picon.presentation.search.SearchViewModel
import com.yapp.picon.presentation.simplejoin.SimpleJoinViewModel
import com.yapp.picon.presentation.splash.SplashViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { SimpleJoinViewModel(get()) }
    viewModel { SearchViewModel(get(), get(), get(), get()) }
    viewModel { LoginViewModel(get(), get()) }
    viewModel { MapViewModel(get(), get()) }
    viewModel { SplashViewModel(get(), get(), get()) }
    viewModel { PostViewModel(get(), get()) }
    viewModel { UserInfoViewModel(get()) }
    viewModel { PinGalleryViewModel(get(), get()) }
    viewModel { MyProfileViewModel(get(), get(), get(), get()) }
    viewModel { PostDetailViewModel(get()) }
    viewModel { CollectViewModel(get()) }
    viewModel { ManageFriendViewModel() }
    viewModel { NavViewModel(get(), get(), get()) }
    viewModel { FriendProfileViewModel(get()) }
    viewModel { EditPostViewModel(get(), get()) }
}