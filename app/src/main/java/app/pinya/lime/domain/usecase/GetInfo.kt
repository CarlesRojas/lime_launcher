package app.pinya.lime.domain.usecase

import app.pinya.lime.data.repo.InfoRepo
import app.pinya.lime.domain.model.InfoModel
import javax.inject.Inject

class GetInfo @Inject constructor(
    private val infoRepo: InfoRepo
) {
    suspend operator fun invoke(): InfoModel {
        return infoRepo.getInfoFromMemory()
    }
}