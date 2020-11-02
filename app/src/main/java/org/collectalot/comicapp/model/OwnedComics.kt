package org.collectalot.comicapp.model

import io.realm.RealmObject;
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class comic_owned_comics(
    var condition: String? = null,
    var price: comic_owned_comics_price? = null
): RealmObject() {}
