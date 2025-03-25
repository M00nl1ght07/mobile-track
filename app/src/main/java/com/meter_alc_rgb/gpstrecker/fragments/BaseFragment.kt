package com.meter_alc_rgb.gpstrecker.fragments

import androidx.fragment.app.Fragment

/**
 * Базовый класс для всех фрагментов приложения.
 * Наследуется от стандартного класса Fragment и предоставляет общую функциональность.
 *
 * @param name Имя фрагмента, используется для идентификации и логирования
 */
open class BaseFragment(var name: String) : Fragment() {}