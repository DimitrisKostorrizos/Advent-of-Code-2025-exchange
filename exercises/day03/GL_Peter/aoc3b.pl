use List::Util qw( min max );

my $totalscore = 0;
while (<>) {
	chop();
# split the string $_ into an array of charcters
  my @bank = split //, $_;
  my $val = "";
    my $p = -1;
  for (my $d = 0 ; $d < 12 ; $d++ ) {
    my $max = 0;
    for (my $i = $p + 1; $i + 11 - $d < @bank; $i++) {
      if ($bank[$i] > $max) {
        $p = $i;
        $max = $bank[$i];
      }
    }
  print " $bank[$p]";
    $val = $val.$bank[$p];
  }
  print " $val\n";
  $totalscore += $val;
}

print "\n== $totalscore\n";
