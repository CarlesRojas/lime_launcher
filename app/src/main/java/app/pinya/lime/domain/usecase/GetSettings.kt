package app.pinya.lime.domain.usecase

import app.pinya.lime.data.repo.SettingsRepo
import app.pinya.lime.domain.model.SettingsModel
import javax.inject.Inject

class GetSettings @Inject constructor(
    private val settingsRepo: SettingsRepo
) {
    operator fun invoke(): SettingsModel {
        return settingsRepo.getSettingsFromMemory()
    }
}