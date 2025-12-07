use List::Util qw( min max );
my $totalscore = 0;

$_ = <>;
chop();
my @ranges = split(",");


for my $range ( @ranges )  {
  my ($firstId,$lastId) = ($range =~ m/(\d+)\-(\d+)/);
  my $len = 0;
  my @splits = ();
  for (my $id=$firstId ; $id <= $lastId ; $id++) {
    if (length($id) != $len) {
      $len = length($id);
      @splits = ();
      for (my $i =1 ; $i <$len ; $i++) {
        if (($len % $i) == 0) { 
          push(@splits, $i);
        }
      }
    }
    for $s ( @splits) {
      my $pat = substr($id,0,$s);
      my $ok=1;
      for (my $j=$s ; $j<$len && $ok; $j+=$s ) {
        $ok = (substr($id,($j),$s) == $pat) ;
      } 
      $ok && ( $totalscore += $id) &&  last;
    }
  }
}

print "\n== $totalscore\n";
