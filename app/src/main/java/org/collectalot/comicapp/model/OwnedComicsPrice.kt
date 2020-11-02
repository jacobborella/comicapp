package org.collectalot.comicapp.model

import io.realm.RealmObject;
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class comic_owned_comics_price(
    var currency: String? = null,
    var value: String? = null
): RealmObject() {}
