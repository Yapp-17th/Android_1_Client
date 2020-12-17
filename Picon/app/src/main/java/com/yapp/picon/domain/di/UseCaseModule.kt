package com.yapp.picon.domain.di

import com.yapp.picon.domain.usecase.*
import org.koin.dsl.module

val useCaseModule = module {
    single {
        GetLocalUseCase(get())
    }
    single {
        GetAllSearedUseCase(get())
    }
    single {
        InsertSearedUseCase(get())
    }
    single {
        DeleteSearedUseCase(get())
    }
    single {
        SimpleJoinUseCase(get())
    }
    single {
        LoginUseCase(get())
    }
    single {
        SaveAccessTokenUseCase(get())
    }
    single {
        LoadAccessTokenUseCase(get())
    }
    single {
        LogoutUseCase(get())
    }
    single {
        UploadImageUseCase(get(), get())
    }
    single {
        CreatePostUseCase(get(), get())
    }
    single {
        RequestPostsUseCase(get(), get())
    }
    single {
        LoadFirstUseCase(get())
    }
    single {
        SaveFirstUseCase(get())
    }
    single {
        GetRevGeoUseCase(get())
    }
    single {
        RemovePostUseCase(get(), get())
    }
    single {
        GetUserInfoUseCase(get())
    }
    single {
        UploadProfileUseCase(get())
    }
    single {
        GetStatisticUseCase(get(), get())
    }
    single {
        UpdatePostUseCase(get(), get())
    }
}