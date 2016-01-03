class WordCounter
	private
	def self.parse_text text
		text.gsub!(/[^a-zA-Z0-9]/,' ')
	end

	def self.get_strings_c text
		strings = text.scan(/".*?"/)
		text.gsub!(/".*?"/,' ')
		strings
	end
	def self.parse_c text
		#Still doesn't parse strings right
		text.gsub!(/[-+=*\/;<>\(\){}&|,:\?\"\'\[\]\!]/,' ').gsub!(/\s[0-9][^\s]*/,'').gsub!(/\./,' ')
	end

	public

	def self.sort_words words
		words.sort_by { |word, times| [-times, word] }
	end

	def self.count_words text, ext, words
		strings = Array.new
		if ext == 'cpp' || ext == 'cc' || ext == 'c'
			strings = get_strings_c text
			parse_c text
		else
			parse_text text
		end
	  	text.downcase!
		text.split(' ').each do |word|
		  	words[word] = 0 if words[word]==nil
		    words[word] += 1
		end
		strings.each do |word|
		  	words[word] = 0 if words[word]==nil
		    words[word] += 1
		end
	end
end