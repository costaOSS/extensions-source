#!/bin/bash
OUTPUT_FILE="mega-app/src/main/java/eu/kanade/tachiyomi/megaextension/MegaExtensionFactory.kt"
mkdir -p $(dirname $OUTPUT_FILE)

echo "package eu.kanade.tachiyomi.megaextension" > $OUTPUT_FILE
echo "" >> $OUTPUT_FILE
echo "import eu.kanade.tachiyomi.source.SourceFactory" >> $OUTPUT_FILE
echo "import eu.kanade.tachiyomi.source.Source" >> $OUTPUT_FILE
echo "" >> $OUTPUT_FILE
echo "class MegaExtensionFactory : SourceFactory {" >> $OUTPUT_FILE
echo "    override fun createSources(): List<Source> {" >> $OUTPUT_FILE
echo "        val sources = mutableListOf<Source>()" >> $OUTPUT_FILE

# Iterate over all extensions
for lang_dir in src/*/; do
    if [ ! -d "$lang_dir" ]; then continue; fi
    lang=$(basename "$lang_dir")
    for ext_dir in "$lang_dir"*/; do
        if [ ! -d "$ext_dir" ]; then continue; fi
        ext=$(basename "$ext_dir")
        
        gradle_file="$ext_dir/build.gradle.kts"
        if [ ! -f "$gradle_file" ]; then continue; fi
        
        # Check if it uses className
        classname=$(grep "className =" "$gradle_file" | sed 's/.*className = "\(.*\)".*/\1/' | head -n 1)
        
        # Determine fully qualified name
        if [ -n "$classname" ]; then
            fqn="eu.kanade.tachiyomi.extension.$lang.$ext.$classname"
        else
            fqn="eu.kanade.tachiyomi.extension.$lang.$ext.ExtensionGenerated"
        fi
        
        # We need to instantiate it. But wait! If it's a SourceFactory, we call createSources().
        # If it's a Source, we just instantiate it.
        # How do we know if it's a SourceFactory or Source?
        # Let's use a safe try-catch wrapper in Kotlin!
        echo "        try {" >> $OUTPUT_FILE
        echo "            val instance = Class.forName(\"$fqn\").getDeclaredConstructor().newInstance()" >> $OUTPUT_FILE
        echo "            if (instance is SourceFactory) {" >> $OUTPUT_FILE
        echo "                sources.addAll(instance.createSources())" >> $OUTPUT_FILE
        echo "            } else if (instance is Source) {" >> $OUTPUT_FILE
        echo "                sources.add(instance)" >> $OUTPUT_FILE
        echo "            }" >> $OUTPUT_FILE
        echo "        } catch (e: Exception) {" >> $OUTPUT_FILE
        echo "            // Ignore or log" >> $OUTPUT_FILE
        echo "        }" >> $OUTPUT_FILE
    done
done

echo "        return sources" >> $OUTPUT_FILE
echo "    }" >> $OUTPUT_FILE
echo "}" >> $OUTPUT_FILE

chmod +x generate_mega_factory.sh
./generate_mega_factory.sh
