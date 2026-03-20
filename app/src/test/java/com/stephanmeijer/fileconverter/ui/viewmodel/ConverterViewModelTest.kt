package com.stephanmeijer.fileconverter.ui.viewmodel

import com.google.common.truth.Truth.assertThat
import com.stephanmeijer.fileconverter.engine.ConversionResult
import org.junit.jupiter.api.Test

class ConverterViewModelTest {

    @Test
    fun `Converting default progress is indeterminate (-1f)`() {
        val converting = ConversionState.Converting()
        assertThat(converting.progress).isEqualTo(-1f)
    }

    @Test
    fun `Converting with explicit progress stores value`() {
        val converting50 = ConversionState.Converting(0.5f)
        assertThat(converting50.progress).isEqualTo(0.5f)
    }

    @Test
    fun `Converting data class equality by progress`() {
        assertThat(ConversionState.Converting()).isEqualTo(ConversionState.Converting(-1f))
        assertThat(ConversionState.Converting(0.5f)).isNotEqualTo(ConversionState.Converting(0.75f))
    }

    @Test
    fun `Cancelled is a singleton object`() {
        assertThat(ConversionState.Cancelled).isEqualTo(ConversionState.Cancelled)
        assertThat(ConversionState.Cancelled).isSameInstanceAs(ConversionState.Cancelled)
    }

    @Test
    fun `Idle is a singleton object`() {
        assertThat(ConversionState.Idle).isEqualTo(ConversionState.Idle)
    }

    @Test
    fun `Success holds result`() {
        val result = ConversionResult(outputBytes = ByteArray(0), output = "", warnings = emptyList(), error = null)
        val success = ConversionState.Success(result)
        assertThat(success.result).isSameInstanceAs(result)
    }

    @Test
    fun `updateConversionState to Converting() sets indeterminate state`() {
        val vm = ConverterViewModel()
        vm.updateConversionState(ConversionState.Converting())
        assertThat(vm.conversionState).isInstanceOf(ConversionState.Converting::class.java)
        assertThat((vm.conversionState as ConversionState.Converting).progress).isEqualTo(-1f)
    }

    @Test
    fun `updateConversionState to Converting(0_5f) sets 50% progress`() {
        val vm = ConverterViewModel()
        vm.updateConversionState(ConversionState.Converting(0.5f))
        assertThat((vm.conversionState as ConversionState.Converting).progress).isEqualTo(0.5f)
    }

    @Test
    fun `updateConversionState to Cancelled sets Cancelled`() {
        val vm = ConverterViewModel()
        vm.updateConversionState(ConversionState.Cancelled)
        assertThat(vm.conversionState).isEqualTo(ConversionState.Cancelled)
    }

    @Test
    fun `initial conversionState is Idle`() {
        val vm = ConverterViewModel()
        assertThat(vm.conversionState).isEqualTo(ConversionState.Idle)
    }

    @Test
    fun `cancelConversion sets state to Cancelled`() {
        val vm = ConverterViewModel()
        vm.updateConversionState(ConversionState.Converting())
        vm.cancelConversion()
        assertThat(vm.conversionState).isEqualTo(ConversionState.Cancelled)
    }

    @Test
    fun `cancelConversion with null job does not throw`() {
        val vm = ConverterViewModel()
        vm.conversionJob = null
        vm.cancelConversion()
        assertThat(vm.conversionState).isEqualTo(ConversionState.Cancelled)
    }

    @Test
    fun `canConvert is false when state is Converting`() {
        val vm = ConverterViewModel()
        vm.updateConversionState(ConversionState.Converting())
        assertThat(vm.canConvert).isFalse()
    }

    @Test
    fun `canConvert is false when state is Cancelled`() {
        val vm = ConverterViewModel()
        vm.updateConversionState(ConversionState.Cancelled)
        assertThat(vm.canConvert).isFalse()
    }
}
