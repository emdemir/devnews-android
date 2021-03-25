package org.devnews.android.repository.objects

data class Tag(
    val name: String,
    val description: String
) {
    override fun toString(): String {
        // This is called by ArrayAdapter's filtering. Let's return the tag's name.
        return name
    }
}