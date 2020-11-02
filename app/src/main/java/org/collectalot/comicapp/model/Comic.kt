package org.collectalot.comicapp.model

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import org.bson.types.ObjectId;

open class comic(
    @PrimaryKey var _id: ObjectId? = null,
    var country: String? = null,
    var edition: String? = null,
    //@Required
    var owned_comics: RealmList<comic_owned_comics> = RealmList(),
    var owner_id: String? = null,
    var publisher: String? = null,
    var subtitle: String? = null,
    var title: String? = null,
    var year: String? = null
): RealmObject() {}
