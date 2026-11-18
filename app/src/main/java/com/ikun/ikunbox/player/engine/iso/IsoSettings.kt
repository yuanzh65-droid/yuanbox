package com.ikun.ikunbox.player.engine.iso

import com.ikun.ikunbox.util.KVKeys
import com.ikun.ikunbox.util.KV

object IsoSettings {

    var lpcmEnabled: Boolean
        get() = KV.get(KVKeys.ISO_LPCM, KVKeys.ISO_LPCM_DEFAULT)
        set(value) {
            KV.put(KVKeys.ISO_LPCM, value)
        }

    var trueHdCoreEnabled: Boolean
        get() = KV.get(KVKeys.ISO_TRUEHD_CORE, KVKeys.ISO_TRUEHD_CORE_DEFAULT)
        set(value) {
            KV.put(KVKeys.ISO_TRUEHD_CORE, value)
        }

    var pgsEnabled: Boolean
        get() = KV.get(KVKeys.ISO_PGS, KVKeys.ISO_PGS_DEFAULT)
        set(value) {
            KV.put(KVKeys.ISO_PGS, value)
        }
}
