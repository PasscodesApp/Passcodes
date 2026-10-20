// This file is required for Expo/React Native SQLite migrations - https://orm.drizzle.team/quick-sqlite/expo

import journal from './meta/_journal.json';
import m0000 from './0000_init_after_migrating_from_room_android.sql';
import m0001 from './0001_add_categories.sql';
import m0002 from './0002_created_at_and_updated_at_not_nullable.sql';

  export default {
    journal,
    migrations: {
      m0000,
m0001,
m0002
    }
  }
  