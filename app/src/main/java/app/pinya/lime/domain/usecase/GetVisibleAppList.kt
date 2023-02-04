package app.pinya.lime.domain.usecase

import app.pinya.lime.domain.model.AppModel
import app.pinya.lime.data.repo.AppRepo
import javax.inject.Inject

class GetVisibleAppList @Inject constructor(
    private val appRepo: AppRepo
) {
    suspend operator fun invoke(): MutableList<AppModel> = appRepo.getAppList()
}