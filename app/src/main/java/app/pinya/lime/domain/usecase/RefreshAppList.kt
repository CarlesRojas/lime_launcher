package app.pinya.lime.domain.usecase

import app.pinya.lime.data.repo.AppRepo
import app.pinya.lime.domain.model.AppModel
import javax.inject.Inject

class RefreshAppList @Inject constructor(
    private val appRepo: AppRepo
) {
    suspend operator fun invoke(): MutableList<AppModel> {
        val appList = appRepo.getAppList()

        return appList
    }
}