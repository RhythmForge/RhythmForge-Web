**RhythmForge-Web:**
The web frontend of RhythmForge based on Shiina for **bancho.py-ex**

### **Requirements**
> ⚠️ Your bancho server needs to be [RhythmForge](https://github.com/RhythmForge/RhythmForge) at the moment. Everything else will break

### **Installation:**
1. `make install` - will install and compile the project
2. edit files in `config/` and replace all filenames with `.example`
3. `make run` - to start the frontend

### **Feature List:**

- ✅ Leaderboards based on API
- ✅ Country Leaderboards based on API
- ✅ Homepage with statistics
- ✅ Clan Leaderboard with some compatitive statistics
- ✅ Clan Page
- ✅ Search
- ✅ User Page (w. first places, best/last scores, playcount graph and achievements)
- ✅ Authorization
- ✅ Beatmap Search page
- ✅ Profile picture change
- ❌ Profile Banner change
- ✅ Theming
- ✅ Admin Panel (expandable)
- ✅ Userpage w/ edit
- 🧩 Java Plugin Loader w/ Events
- 🧩 SEO
- 🧩 Donate
- ❌ Beatmap Request
- ❌ Sitemap Generation


### **Technical:**

- ✅ API Request Caching
- ✅ Error/Request Logger with configuration at `.config/logger.env`
- ✅ Easy customization with `.config/customization.yml`
