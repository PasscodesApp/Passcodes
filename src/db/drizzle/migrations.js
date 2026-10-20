// This file is required for Expo/React Native SQLite migrations - https://orm.drizzle.team/quick-sqlite/expo

import journal from './meta/_journal.json';
import m0000 from './0000_init_after_migrating_from_room_android.sql';
import m0001 from './0001_v2_unixtimestamp_softdelete_applictionids.sql';

  export default {
    journal,
    migrations: {
      m0000,
m0001
    }
  }
  