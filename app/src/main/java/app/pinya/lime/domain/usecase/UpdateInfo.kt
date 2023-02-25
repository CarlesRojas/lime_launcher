package app.pinya.lime.domain.usecase

import app.pinya.lime.data.repo.InfoRepo
import app.pinya.lime.domain.model.InfoModel
import javax.inject.Inject

class UpdateInfo @Inject constructor(
    private val infoRepo: InfoRepo
) {
    operator fun invoke(newInfo: InfoModel) {
        return infoRepo.setInfoToMemory(newInfo)
    }
}