package com.kitsune.app.ui.components.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kitsune.app.R
import com.kitsune.app.ui.theme.KitsuneTheme

@Composable
fun KitsuneTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    singleLine: Boolean = true,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Uri,
        imeAction = ImeAction.Go
    )
) {
    val shape = RoundedCornerShape(KitsuneTheme.shapes.inputRadius)

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .background(KitsuneTheme.colors.surfaceVariant, shape)
            .border(1.dp, KitsuneTheme.colors.borderSubtle, shape)
            .padding(
                horizontal = KitsuneTheme.spacing.md,
                vertical = KitsuneTheme.spacing.md
            ),
        singleLine = singleLine,
        textStyle = KitsuneTheme.typography.bodyLarge.copy(
            color = KitsuneTheme.colors.textPrimary
        ),
        cursorBrush = SolidColor(KitsuneTheme.colors.accentCyan),
        keyboardActions = keyboardActions,
        keyboardOptions = keyboardOptions,
        decorationBox = { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (leadingIcon != null) {
                    Box(modifier = Modifier.padding(end = KitsuneTheme.spacing.sm)) {
                        leadingIcon()
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(
                            text = placeholder,
                            style = KitsuneTheme.typography.bodyLarge,
                            color = KitsuneTheme.colors.textMuted
                        )
                    }
                    innerTextField()
                }

                if (trailingIcon != null) {
                    Box(modifier = Modifier.padding(start = KitsuneTheme.spacing.sm)) {
                        trailingIcon()
                    }
                }
            }
        }
    )
}

@Preview(name = "KitsuneTextField Preview")
@Composable
private fun KitsuneTextFieldPreview() {
    KitsuneTheme {
        KitsuneTextField(
            value = "",
            onValueChange = {},
            placeholder = stringResource(R.string.hint_url_input)
        )
    }
}
